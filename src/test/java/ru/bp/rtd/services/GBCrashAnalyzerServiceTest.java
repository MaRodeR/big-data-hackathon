package ru.bp.rtd.services;


import org.apache.spark.sql.Row;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GBCrashAnalyzerServiceTest {

    @Autowired
    private GBCrashAnalyzerService crashAnalyzerService;

    @Test
    public void getMostDangerousCars() throws Exception {
        String filePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/2015_Make_Model.csv").getFile();
        List<Row> maxDangerousCarModels = crashAnalyzerService.getMostDangerousCars(filePath);
        assertEquals(10, maxDangerousCarModels.size());
    }

    @Test
    public void getCrashesCountByDriversAge() throws Exception {
        String filePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/2015_Vehicles_short.csv").getFile();
        Map<Integer, Integer> result = crashAnalyzerService.getCrashesCountByDriversAge(filePath);
        assertEquals(result.keySet().size(), 10);
        assertEquals(result.get(0), valueOf(1));
        assertEquals(result.get(10), valueOf(9));
        assertEquals(result.get(20), valueOf(54));
        assertEquals(result.get(30), valueOf(65));
        assertEquals(result.get(40), valueOf(42));
        assertEquals(result.get(50), valueOf(24));
        assertEquals(result.get(60), valueOf(13));
        assertEquals(result.get(70), valueOf(4));
        assertEquals(result.get(80), valueOf(1));
        assertEquals(result.get(90), valueOf(1));
    }

}
