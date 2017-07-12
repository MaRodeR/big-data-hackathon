package ru.bp.rtd.services;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

@Service
public class GBCrashAnalyzerService {

    @Autowired
    private SparkSession spark;

    public List<Row> getMostDangerousCars(String file) {
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(file);
        dataset.printSchema();
        return dataset.groupBy(col("make")).count().orderBy(desc("count")).limit(10).collectAsList();
    }


    public Map<Integer, Integer> getCrashesCountByDriversAge(String vehiclesFile) {
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(vehiclesFile);
        dataset.printSchema();
        Dataset<Row> ageRows = dataset.select(col("Age_of_Driver")).filter(row -> row.getInt(0) > 0);
        return ageRows.toJavaRDD().map(row -> ((row.getInt(0) / 10) * 10))
                .mapToPair(age -> new Tuple2<>(age, 1))
                .reduceByKey((a, b) -> a + b)
                .collectAsMap();
    }

}
