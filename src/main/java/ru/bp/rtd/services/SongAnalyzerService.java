package ru.bp.rtd.services;

import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service
public class SongAnalyzerService implements Serializable {

    @Autowired
    private JavaSparkContext javaSparkContext;

    public List<String> getWords(String lyricsFile) {
        return javaSparkContext.textFile(lyricsFile)
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator())
                .map(String::toLowerCase)
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b)
                .mapToPair(Tuple2::swap)
                .sortByKey(false)
                .map(Tuple2::_2)
                .collect();
    }
}
