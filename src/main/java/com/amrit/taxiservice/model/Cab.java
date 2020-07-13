package com.amrit.taxiservice.model;

public class Cab {

    double latitude;
    double longitude;
    String registrationNo;
    boolean isBooked;

    public Cab(String registrationNo) {
        this.registrationNo = registrationNo;
        this.isBooked = false;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
}
