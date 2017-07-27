package ru.bp.rtd.services;


import org.apache.parquet.it.unimi.dsi.fastutil.longs.LongComparator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bp.rtd.domain.CarCrash;
import ru.bp.rtd.domain.CrashGroup;
import ru.bp.rtd.utils.PointUtils;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;

@Service
public class GBCrashAnalyzerService {

    @Autowired
    private SparkSession spark;

    @Cacheable("mostDangerousCars")
    public List<Row> getMostDangerousCars(String file) {
        Dataset<Row> dataset = getDataSetByCsv(file);
        dataset.printSchema();
        return dataset.groupBy(col("make")).count().orderBy(desc("count")).limit(10).collectAsList();
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

    @Cacheable("crashesByHourOfDay")
    public List<CarCrash> getCrashesByHourOfDay(String accidentsFile, int hour) {
        String hourValue = (hour < 10 ? "0" : "") + String.valueOf(hour);

        Dataset<Row> dataSet = getDataSetByCsv(accidentsFile);
        dataSet.printSchema();
        dataSet.show(10);
        List<Row> rows = dataSet
                .filter(col("Time").startsWith(hourValue))
                .limit(100).collectAsList();


        return rows.stream().map(row -> new CarCrash()
                .setId(row.getString(0))
                .setLongitude(row.getDouble(3))
                .setLatitude(row.getDouble(4))
                .setPoliceOfficerAttendance(row.getInt(30)))
                .collect(toList());
    }

//    @Cacheable("groupCrashesByHourOfDay")
    public List<CrashGroup> getGroupCrashesByHourOfDay(String accidentsFile, int hour) {
        String hourValue = (hour < 10 ? "0" : "") + String.valueOf(hour);

        Dataset<Row> dataSet = getDataSetByCsv(accidentsFile);
        dataSet.printSchema();
        dataSet.show(10);
        List<Row> rows = dataSet
                .filter(col("Time").startsWith(hourValue))
                .collectAsList();


        List<CarCrash> crashes = rows.stream().map(row -> new CarCrash()
                .setId(row.getString(0))
                .setLongitude(row.getDouble(3))
                .setLatitude(row.getDouble(4)))
                .collect(toList());

        double maxDistance = 1;
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

            return new CrashGroup()
                    .setCrashCount(crashList.size())
                    .setCenterLatitude(avgLatutude)
                    .setCenterLongitude(avgLongitude);
        }).collect(toList());
    }

    private Dataset<Row> getDataSetByCsv(String accidentsFile) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(accidentsFile);
    }

}
