package ru.bp.rtd.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CarSalesAnalyzerServiceTest {

    @Autowired
    private CarSalesAnalyzerService carSalesAnalyzerService;

    @Test
    public void getCarCountBy() throws Exception {
        int bmwCarCount = carSalesAnalyzerService.getCarCountBy("bmw");

        System.out.println(bmwCarCount);
    }

}