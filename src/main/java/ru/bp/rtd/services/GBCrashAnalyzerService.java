package ru.bp.rtd.services;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.bp.rtd.domain.CarCrash;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

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

        List<Row> rows = getDataSetByCsv(accidentsFile)
                .filter(col("Time").startsWith(hourValue))
                .limit(100).collectAsList();

        return rows.stream().map(row -> new CarCrash()
                .setId(row.getString(0))
                .setLongitude(row.getDouble(3))
                .setLatitude(row.getDouble(4)))
                .collect(Collectors.toList());
    }

    private Dataset<Row> getDataSetByCsv(String accidentsFile) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(accidentsFile);
    }

}
