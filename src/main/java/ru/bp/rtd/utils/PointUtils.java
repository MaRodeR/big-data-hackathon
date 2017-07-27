package ru.bp.rtd.utils;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class PointUtils {

    public static double getDistance(double latitudeStart, double longitudeStart, double latitudeDest, double longitudeDest) {
        return sqrt(pow(latitudeStart - latitudeDest, 2) + pow(longitudeStart - longitudeDest, 2));
    }
}
