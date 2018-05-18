package com.example.minh.nckh.Model;

import java.io.Serializable;

/**
 * Created by LaVanDuc on 4/14/2018.
 */

public class ToaDo implements Serializable {
    private double Lat;
    private double lng;

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public ToaDo(double lat, double lng) {

        Lat = lat;
        this.lng = lng;
    }
}
