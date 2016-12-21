package org.tud.mensadresden.finding.offers.service;

import android.content.Context;
import android.location.Location;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.tud.mensadresden.finding.offers.model.Mensa;
import org.tud.mensadresden.finding.offers.repository.MensaRepository;
import org.tud.mensadresden.service.LocationService;
import org.tud.mensadresden.service.NetworkService;

import java.util.List;

public class MensaApiService {
    private Context context;

    @Override
    public List<Mensa> findAllOrderedByDistance() {
        LocationService locationService = LocationService.getInstance();
        Location lastLocation = locationService.getMostRecentLocation();
        if (lastLocation == null) {
            locationService.requestLocationUpdates();
        }
        String uri = "http://openmensa.org/api/v2/canteens";
        if (lastLocation != null) {
            uri += String.format(
                    "?near[lat]=%1$s&near[lng]=%2$s&near[dist]=%3$s&hasCoordinates=true",
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude(),
                    5000000
            );
        } else {
            uri += "?hasCoordinates=true";
        }
        NetworkService.getInstance(context).addToRequestQueue(
                new JsonArrayRequest(
                        Request.Method.GET,
                        uri,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                mostRecentlyFetchedJobs = gson.fromJson(
                                        response.toString(),
                                        new TypeToken<List<Job>>() {
                                        }.getType()
                                );
                                buildJobMap();
                                writeJobsToCache(context);
                                if (callback != null) {
                                    callback.onSuccess();
                                }
                                for (MensaService.JobOffersUpdateListener listener : listeners) {
                                    listener.onJobOffersUpdate(mostRecentlyFetchedJobs);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (callback != null) {
                            callback.onFail(MensaService.JobOffersRefreshError.NETWORK_ERROR, error);
                        }
                    }
                }));

        return null;
    }
}
