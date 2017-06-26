package ru.bp.rtd.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class CrashAnalyzerServiceTest {

    @Autowired
    private CrashAnalyzerService crashAnalyzerService;

    @Test
    public void getCrashCount() throws Exception {
        long counts = crashAnalyzerService.getCrashCount("/Users/ArtemKaravaev/Documents/projects/big_data/2017-crash.json");
        assertEquals(13587, counts);
    }

}