package ru.bp.rtd.services;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.spark.sql.functions.desc;

@Service
public class CarSalesAnalyzerService {

    @Autowired
    private JavaSparkContext javaSparkContext;
    @Autowired
    private SparkSession spark;

    public long getCarCountBy(String file, String brand){
        Dataset<Row> dataset = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(file);
        dataset.show();
        dataset.printSchema();
        dataset.createOrReplaceTempView("cars");
        Dataset<Row> cars = spark.sql("SELECT * FROM cars WHERE name LIKE '%" + brand + "%'")
                .sort(desc("price"));
        cars.show(1000);
        return cars.count();
    }
}
