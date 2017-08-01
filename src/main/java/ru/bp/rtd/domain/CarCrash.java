package ru.bp.rtd.domain;

import java.io.Serializable;

public class CarCrash implements Serializable {

    private String id;
    private double latitude;
    private double longitude;
    private int policeOfficerAttendance;
    private int severity;

    public String getId() {
        return id;
    }

    public CarCrash setId(String id) {
        this.id = id;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public CarCrash setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public CarCrash setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public int isPoliceForce() {
        return policeOfficerAttendance;
    }

    public CarCrash setPoliceOfficerAttendance(int policeOfficerAttendance) {
        this.policeOfficerAttendance = policeOfficerAttendance;
        return this;
    }

    public int getSeverity() {
        return severity;
    }

    public CarCrash setSeverity(int severity) {
        this.severity = severity;
        return this;
    }
}
