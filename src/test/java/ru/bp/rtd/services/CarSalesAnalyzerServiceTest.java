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
        String file = this.getClass().getResource("used-cars-short.csv").getFile();
        long carCount = carSalesAnalyzerService.getCarCountBy(file,"Golf");
        assertEquals(1, carCount);
    }

    @Test
    public void getCarCountByLarge() throws Exception {
        String file = this.getClass().getResource("autos.csv").getFile();
        long carCount = carSalesAnalyzerService.getCarCountBy(file,"BMW");
        assertEquals(33285, carCount);
    }

}