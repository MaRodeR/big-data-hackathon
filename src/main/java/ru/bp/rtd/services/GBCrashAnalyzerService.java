package ru.bp.rtd.services;


import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bp.rtd.domain.CarCrash;
import ru.bp.rtd.domain.CrashGroup;
import ru.bp.rtd.domain.Point;
import ru.bp.rtd.domain.EngineCasualtiesCrash;
import ru.bp.rtd.utils.PointUtils;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;

@Service
public class GBCrashAnalyzerService {

    private Map<Integer, String> roadTypes = new HashMap<>();

    {
        roadTypes.put(1, "Roundabout");
        roadTypes.put(2, "One way street");
        roadTypes.put(3, "Dual carriageway");
        roadTypes.put(6, "Single carriageway");
        roadTypes.put(7, "Slip road");
        roadTypes.put(9, "Unknown");
        roadTypes.put(12, "One way street/Slip road");
    }

    private Map<Integer, String> vehicleTypes = new HashMap<>();

    {
        vehicleTypes.put(1, "Pedal cycle");
        vehicleTypes.put(2, "Motorcycle 50cc and under");
        vehicleTypes.put(3, "Motorcycle 125cc and under");
        vehicleTypes.put(4, "Motorcycle over 125cc and up to 500cc");
        vehicleTypes.put(5, "Motorcycle over 500cc");
        vehicleTypes.put(8, "Taxi/Private hire car");
        vehicleTypes.put(9, "Car");
        vehicleTypes.put(10, "Minibus (8 - 16 passenger seats)");
        vehicleTypes.put(11, "Bus or coach (17 or more pass seats)");
        vehicleTypes.put(16, "Ridden horse");
        vehicleTypes.put(17, "Agricultural vehicle");
        vehicleTypes.put(18, "Tram");
        vehicleTypes.put(19, "Van / Goods 3.5 tonnes mgw or under");
        vehicleTypes.put(20, "Goods over 3.5t. and under 7.5t");
        vehicleTypes.put(21, "Goods 7.5 tonnes mgw and over");
        vehicleTypes.put(22, "Mobility scooter");
        vehicleTypes.put(23, "Electric motorcycle");
        vehicleTypes.put(90, "Other vehicle");
        vehicleTypes.put(97, "Motorcycle - unknown cc");
        vehicleTypes.put(98, "Goods vehicle - unknown weight");
    }

    @Autowired
    private SparkSession spark;

    @Cacheable("mostDangerousCars")
    public List<Row> getMostDangerousCars(String file, int limit) {
        Dataset<Row> dataset = getDataSetByCsv(file);
        dataset.printSchema();
        return dataset.groupBy(col("make")).count().orderBy(desc("count")).limit(limit).collectAsList();
    }

    @Cacheable("crashesCountByCarMake")
    public long getCrashesCountByCarMake(String file, String make) {
        Dataset<Row> dataset = getDataSetByCsv(file);
        dataset.printSchema();

        Dataset<Row> makeCrashes = dataset.filter(col("make").like("%" + make + "%"));
        makeCrashes.show();
        return makeCrashes.count();
    }


    @Cacheable("crashesCountByDriversAge")
    public Map<Integer, Integer> getCrashesCountByDriversAge(String vehiclesFile) {
        Dataset<Row> dataset = getDataSetByCsv(vehiclesFile);
        dataset.printSchema();
        Dataset<Row> ageRows = dataset.select(col("Age_of_Driver")).filter(row -> row.getInt(0) > 0);
        return ageRows.toJavaRDD().map(row -> ((row.getInt(0) / 10) * 10))
                .mapToPair(age -> new Tuple2<>(age, 1))
                .reduceByKey((a, b) -> a + b)
                .collectAsMap();
    }

    @Cacheable("crashesByRoadTypeAndDriverAge")
    public List<EngineCasualtiesCrash> getCrashesByRoadTypeAndDriverAge(String makeModelFile, String accidentsFile) {
        Dataset<Row> makeModelDataSet = getDataSetByCsv(makeModelFile);
        Dataset<Row> accidentsDataSet = getDataSetByCsv(accidentsFile);


        List<Row> rows = makeModelDataSet.join(accidentsDataSet, "Accident_Index")
                .filter(col("Vehicle_Type").equalTo(9))
                .select(col("Engine_Capacity_(CC)").divide(500).cast("int").multiply(500).as("capacity"), col("Number_of_Casualties"))
                .filter(col("capacity").lt(4000))
                .filter(col("capacity").isNotNull())
                .filter(col("capacity").notEqual(-1))
                .filter(col("Number_of_Casualties").notEqual(-1))
                .groupBy(col("capacity"), col("Number_of_Casualties"))
                .count()
                .filter(col("count").gt(0))
                .collectAsList();

        return rows.stream()
                .map(row -> new EngineCasualtiesCrash()
                        .setEngineCapacity(String.valueOf(row.getInt(0)))
                        .setNumberOfCasualties(row.getInt(1))
                        .setCrashCount(row.getLong(2)))
                .collect(Collectors.toList());
    }

    @Cacheable("crashesByHourOfDay")
    public List<CarCrash> getCrashesByHourOfDay(String accidentsFile, int hour, Integer severity) {
        String hourValue = (hour < 10 ? "0" : "") + String.valueOf(hour);

        Dataset<Row> dataSet = getDataSetByCsv(accidentsFile);
        dataSet.printSchema();
        dataSet.show(10);
        List<Row> rows = dataSet
                .filter(col("Time").startsWith(hourValue)).filter(col("Accident_Severity").equalTo(severity))
                .collectAsList();


        return rows.stream().map(row -> new CarCrash()
                .setId(row.getString(0))
                .setLongitude(row.getDouble(3))
                .setLatitude(row.getDouble(4))
                .setPoliceOfficerAttendance(row.getInt(30))
                .setSeverity(row.getInt(6)))
                .collect(toList());
    }

    @Cacheable("groupCrashesByHourOfDay")
    public List<CrashGroup> getGroupCrashesByHourOfDay(String accidentsFile, int hour, double maxDistance,
                                                       Point southWestCorner, Point northEastCorner) {
        System.out.println(String.format("get group crashes for hour: %s max distance: %s coordinate: lat [%s, %s], lon [%s, %s]",
                hour, maxDistance,
                southWestCorner != null ? southWestCorner.getLat() : null,
                northEastCorner != null ? northEastCorner.getLat() : null,
                southWestCorner != null ? southWestCorner.getLon() : null,
                northEastCorner != null ? northEastCorner.getLon() : null));
        String hourValue = (hour < 10 ? "0" : "") + String.valueOf(hour);

        Dataset<Row> dataSet = getDataSetByCsv(accidentsFile);
        dataSet.printSchema();
        dataSet.show(10);
        List<Row> rows;
        if (northEastCorner != null && southWestCorner != null) {
            rows = dataSet
                    .filter(col("Time").startsWith(hourValue))
                    .filter(col("Latitude").isNotNull())
                    .filter(col("Longitude").isNotNull())
                    .filter(col("Latitude").between(southWestCorner.getLat(), northEastCorner.getLat()))
                    .filter(col("Longitude").between(southWestCorner.getLon(), northEastCorner.getLon()))
                    .collectAsList();
        } else {
            rows = dataSet
                    .filter(col("Time").startsWith(hourValue))
                    .filter(col("Longitude").isNotNull())
                    .filter(col("Latitude").isNotNull())
                    .collectAsList();
        }


        List<CarCrash> crashes = rows.stream().map(row -> new CarCrash()
                .setId(row.getString(0))
                .setLongitude(row.getDouble(3))
                .setLatitude(row.getDouble(4)))
                .collect(toList());

        List<List<CarCrash>> crashLists = new ArrayList<>();
        crashes.forEach(crash -> {
            List<List<CarCrash>> nearCrashLists = crashLists.parallelStream().filter(crashList ->
                    crashList.parallelStream().allMatch(
                            crashFromList -> PointUtils.getDistance(crash.getLatitude(), crash.getLongitude(),
                                    crashFromList.getLatitude(), crashFromList.getLongitude()) <= maxDistance
                    )
            ).collect(toList());
            if (nearCrashLists.isEmpty()) {
                crashLists.add(singletonList(crash));
            } else {
                crashLists.removeAll(nearCrashLists);
                List<CarCrash> nearCrashes = nearCrashLists.stream().flatMap(Collection::stream).collect(toList());
                List<CarCrash> crashList = new ArrayList<>();
                crashList.addAll(nearCrashes);
                crashList.add(crash);
                crashLists.add(crashList);
            }
        });

        return crashLists.stream().map(crashList -> {
            Double maxLatitude = crashList.stream().map(CarCrash::getLatitude).max(Double::compareTo).get();
            Double minLatitude = crashList.stream().map(CarCrash::getLatitude).min(Double::compareTo).get();
            Double avgLatutude = (maxLatitude + minLatitude) / 2;

            Double maxLongitude = crashList.stream().map(CarCrash::getLongitude).max(Double::compareTo).get();
            Double minLongitude = crashList.stream().map(CarCrash::getLongitude).min(Double::compareTo).get();
            Double avgLongitude = (maxLongitude + minLongitude) / 2;

            Double radius = Stream.of(
                    PointUtils.getDistance(avgLatutude, avgLongitude, maxLatitude, avgLongitude),
                    PointUtils.getDistance(avgLatutude, avgLongitude, minLatitude, avgLongitude),
                    PointUtils.getDistance(avgLatutude, avgLongitude, avgLatutude, maxLongitude),
                    PointUtils.getDistance(avgLatutude, avgLongitude, avgLatutude, minLongitude)
            ).reduce((first, second) -> (first + second) / 2).get();

            return new CrashGroup()
                    .setCrashCount(crashList.size())
                    .setCenterLatitude(avgLatutude)
                    .setCenterLongitude(avgLongitude)
                    .setRadius(radius);
        }).collect(toList());
    }

    @Cacheable("getMaleFemaleStats")
    public Map<Integer, Integer> getMaleFemaleStats(String vehiclesFile, int value, int discriminatorColumnNumber) {
        Dataset<Row> dataset = getDataSetByCsv(vehiclesFile);
        dataset.printSchema();
        Dataset<Row> femaleRows = dataset.filter(col("Sex_of_Driver").equalTo(value)).filter(row -> row.getInt(discriminatorColumnNumber) >= 0);//.filter(col("Skidding_and_Overturning").equalTo(5));

        return femaleRows.toJavaRDD().mapToPair(female -> new Tuple2<>(female.getInt(discriminatorColumnNumber), 1)).
                reduceByKey((a, b) -> a + b).collectAsMap();
    }

    public void loadAndPrint(String file) {
        Dataset<Row> rows = getDataSetByCsv(file);
        rows.printSchema();
        rows.show(5);
    }

    public List<String> getColValue(String file, String columnName) {
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(file);
        dataset.printSchema();

        Encoder<String> stringEncoder = Encoders.STRING();
        Dataset<String> makes = dataset
                .select(col(columnName))
                .distinct()
                .map((MapFunction<Row, String>) row -> row.get(0).toString().trim(), stringEncoder);
        return makes.collectAsList();
    }

    public List<Integer> getIntColValue(String file, String columnName) {
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(file);
        dataset.printSchema();

        Encoder<Integer> intEncoder = Encoders.INT();
        Dataset<Integer> values = dataset
                .select(col(columnName))
                .distinct()
                .map((MapFunction<Row, Integer>) row -> row.getInt(0), intEncoder);
        return values.collectAsList();
    }

    private Dataset<Row> getDataSetByCsv(String accidentsFile) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(accidentsFile);
    }

}
