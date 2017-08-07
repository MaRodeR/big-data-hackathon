package ru.bp.rtd.domain;

public class EngineCasualtiesCrash {

    private String engineCapacity;
    private int numberOfCasualties;
    private long crashCount;

    public String getEngineCapacity() {
        return engineCapacity;
    }

    public EngineCasualtiesCrash setEngineCapacity(String roadType) {
        this.engineCapacity = roadType;
        return this;
    }

    public int getNumberOfCasualties() {
        return numberOfCasualties;
    }

    public EngineCasualtiesCrash setNumberOfCasualties(int numberOfCasualties) {
        this.numberOfCasualties = numberOfCasualties;
        return this;
    }

    public long getCrashCount() {
        return crashCount;
    }

    public EngineCasualtiesCrash setCrashCount(long crashCount) {
        this.crashCount = crashCount;
        return this;
    }
}
