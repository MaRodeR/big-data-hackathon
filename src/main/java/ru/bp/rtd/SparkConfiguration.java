package ru.bp.rtd;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfiguration {

    @Bean
    public JavaSparkContext javaSparkContext() {
        SparkConf sparkConf = new SparkConf().setAppName("Spark sample").setMaster("local[1]");
        return new JavaSparkContext(sparkConf);
    }
}
