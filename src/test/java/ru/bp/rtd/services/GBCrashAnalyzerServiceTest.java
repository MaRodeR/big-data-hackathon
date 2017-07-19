package ru.bp.rtd.services;


import org.apache.spark.sql.Row;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.bp.rtd.domain.CarCrash;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GBCrashAnalyzerServiceTest {

    @Autowired
    private GBCrashAnalyzerService crashAnalyzerService;

    @Test
    public void getMostDangerousCars() throws Exception {
        String filePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/short/2015_Make_Model_short.csv").getFile();
        List<Row> maxDangerousCarModels = crashAnalyzerService.getMostDangerousCars(filePath);
        assertEquals(10, maxDangerousCarModels.size());
    }

    @Test
    public void getCrashesCountByDriversAge() throws Exception {
        String filePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/short/2015_Vehicles_short.csv").getFile();
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

    @Test
    public void getCrashesByHourOfDay() throws Exception {
        String filePath = this.getClass().getClassLoader().getResource("samples/crashes/gb/short/2015_Accidents_short.csv").getFile();
        List<CarCrash> crashes = crashAnalyzerService.getCrashesByHourOfDay(filePath, 0);
        assertNotNull(crashes);
        assertEquals(3, crashes.size());
        List<String> crashesIds = crashes.stream().map(CarCrash::getId).collect(toList());
        assertTrue(crashesIds.containsAll(Arrays.asList("201501BS70039", "201501BS70175", "201501BS70189")));
    }

}
