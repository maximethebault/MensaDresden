package org.tud.mensadresden.finding.offers.model;

import android.location.Location;

import java.io.Serializable;

public class SerializableLocation implements Serializable {
    private double longitude;
    private double latitude;

    public SerializableLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Location toLocation() {
        Location location = new Location("");
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        return location;
    }
}
