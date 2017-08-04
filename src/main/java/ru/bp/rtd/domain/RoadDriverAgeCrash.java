package ru.bp.rtd.domain;

public class RoadDriverAgeCrash {

    private String roadType;
    private int age;
    private long crashCount;

    public String getRoadType() {
        return roadType;
    }

    public RoadDriverAgeCrash setRoadType(String roadType) {
        this.roadType = roadType;
        return this;
    }

    public int getAge() {
        return age;
    }

    public RoadDriverAgeCrash setAge(int age) {
        this.age = age;
        return this;
    }

    public long getCrashCount() {
        return crashCount;
    }

    public RoadDriverAgeCrash setCrashCount(long crashCount) {
        this.crashCount = crashCount;
        return this;
    }
}
