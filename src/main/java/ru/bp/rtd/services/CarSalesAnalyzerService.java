package ru.bp.rtd.services;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarSalesAnalyzerService {

    @Autowired
    private JavaSparkContext javaSparkContext;

    public int getCarCountBy(String file, String brand){
        throw new NotImplementedException("Method is not implemented");
    }
}
