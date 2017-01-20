package org.tud.mensaapp.model.service.global;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationService {
    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

    public interface LocationRequestListener {
        void onLocationRequest();
    }

    private static LocationService mInstance;
    private Location mostRecentLocation;
    private List<LocationUpdateListener> locationUpdatelisteners;
    private LocationRequestListener locationRequestListener;

    private LocationService() {
        locationUpdatelisteners = new ArrayList<>();
    }

    public static synchronized LocationService getInstance() {
        if (mInstance == null) {
            mInstance = new LocationService();
        }
        return mInstance;
    }

    public Location getMostRecentLocation() {
        return mostRecentLocation;
    }

    public void setMostRecentLocation(Location mostRecentLocation) {
        if (mostRecentLocation == null) {
            return;
        }
        this.mostRecentLocation = mostRecentLocation;
        for (LocationUpdateListener listener : locationUpdatelisteners) {
            listener.onLocationUpdate(mostRecentLocation);
        }
    }

    public void requestLocationUpdates() {
        if (this.locationRequestListener != null) {
            this.locationRequestListener.onLocationRequest();
        }
    }

    public void addLocationUpdateListener(LocationUpdateListener locationUpdateListener) {
        locationUpdatelisteners.add(locationUpdateListener);
    }

    public void removeLocationUpdateListener(LocationUpdateListener locationUpdateListener) {
        locationUpdatelisteners.remove(locationUpdateListener);
    }

    public void setLocationRequestListener(LocationRequestListener locationRequestListener) {
        this.locationRequestListener = locationRequestListener;
    }
}