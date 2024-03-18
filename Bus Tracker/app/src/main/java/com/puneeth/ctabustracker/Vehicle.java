package com.puneeth.ctabustracker;

public class Vehicle {

    String vid;
    String lat;
    String lng;

    public Vehicle(String vid,String lat, String lng) {
        this.vid=vid;
        this.lat = lat;
        this.lng = lng;
    }

    public String getVid() {
        return vid;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
