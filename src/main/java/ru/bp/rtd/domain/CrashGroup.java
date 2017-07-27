package ru.bp.rtd.domain;

public class CrashGroup {

    private long crashCount;
    private double centerLatitude;
    private double centerLongitude;

    public long getCrashCount() {
        return crashCount;
    }

    public CrashGroup setCrashCount(long crashCount) {
        this.crashCount = crashCount;
        return this;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public CrashGroup setCenterLatitude(double centerLatitude) {
        this.centerLatitude = centerLatitude;
        return this;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public CrashGroup setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude;
        return this;
    }
}
