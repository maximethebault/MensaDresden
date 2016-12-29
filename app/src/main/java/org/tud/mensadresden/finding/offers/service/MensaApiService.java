package org.tud.mensadresden.finding.offers.service;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.tud.mensadresden.finding.offers.model.Mensa;
import org.tud.mensadresden.service.NetworkService;

import java.lang.reflect.Modifier;
import java.util.List;

public class MensaApiService {
    private static MensaApiService mInstance;

    private Gson gson;

    private MensaApiService() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .excludeFieldsWithModifiers(Modifier.STATIC)// override default so that transient fields are still taken into account
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
    }

    public static synchronized MensaApiService getInstance() {
        if (mInstance == null) {
            mInstance = new MensaApiService();
        }
        return mInstance;
    }

    public void findAllOrderedByDistance(final Context context, Location location, int radius, final FetchMensaListener callback) {
        if (callback != null) {
            callback.onStarted();
        }

        String uri = "http://openmensa.org/api/v2/canteens";
        if (location != null) {
            uri += String.format(
                    "?near[lat]=%1$s&near[lng]=%2$s&near[dist]=%3$s&hasCoordinates=true&per_page=100",
                    location.getLatitude(),
                    location.getLongitude(),
                    radius
            );
        } else {
            uri += String.format(
                    "?near[lat]=%1$s&near[lng]=%2$s&near[dist]=%3$s&hasCoordinates=true&per_page=100",
                    "51.058808",
                    "13.747153",
                    radius
            );
        }
        NetworkService.getInstance(context).addToRequestQueue(
                new JsonArrayRequest(
                        Request.Method.GET,
                        uri,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                List<Mensa> mensas = gson.fromJson(
                                        response.toString(),
                                        new TypeToken<List<Mensa>>() {
                                        }.getType()
                                );
                                if (callback != null) {
                                    callback.onSuccess(true, mensas);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (callback != null) {
                                    callback.onFail(ErrorType.NETWORK_ERROR, error.getMessage());
                                }
                            }
                        })
        );
    }
}
