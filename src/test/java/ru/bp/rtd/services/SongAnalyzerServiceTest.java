package ru.bp.rtd.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SongAnalyzerServiceTest {

    private final Logger logger = LoggerFactory.getLogger(SongAnalyzerServiceTest.class);


    @Autowired
    private SongAnalyzerService songAnalyzerService;

    @Test
    public void getWords() throws Exception {
        String file = this.getClass().getResource("dont_stop_me_now.txt").getFile();
        List<String> words = songAnalyzerService.getWords(file);
        words.forEach(logger::info);
    }
}