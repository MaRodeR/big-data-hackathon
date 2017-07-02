package ru.bp.rtd.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GBCrashAnalyzerServiceTest {

    @Autowired
    private GBCrashAnalyzerService crashAnalyzerService;

    @Test
    public void getCrashCount() throws Exception {
        Object counts = crashAnalyzerService.getCrashCount("C:/Users/810681/Downloads/MakeModel2015/2015_Make_Model.csv");
//        assertEquals(257846, counts);
    }

}
