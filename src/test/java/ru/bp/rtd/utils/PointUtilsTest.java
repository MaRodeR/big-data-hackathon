package ru.bp.rtd.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointUtilsTest {
    @Test
    public void getDistance() throws Exception {
        double distance = PointUtils.getDistance(1, 1, 3, 4);
        assertEquals(3.60, distance, 0.01);
    }

}