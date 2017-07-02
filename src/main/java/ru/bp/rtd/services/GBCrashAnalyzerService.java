package ru.bp.rtd.services;


import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.desc;

@Service
public class GBCrashAnalyzerService {

    @Autowired
    private JavaSparkContext javaSparkContext;
    @Autowired
    private SparkSession spark;

    public List<Row> getCrashCount(String file) {
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(file);
                        //select(explode(col("items")).as("item"));
        dataset.printSchema();
        return dataset.groupBy(col("make")).count().orderBy(desc("count")).limit(10).collectAsList();

//        return dataset.collectAsList();


    }


}
