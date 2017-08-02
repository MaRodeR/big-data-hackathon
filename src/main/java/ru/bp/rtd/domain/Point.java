package ru.bp.rtd.domain;

public class Point {

    private double lat;
    private double lon;

    public Point() {
    }

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
