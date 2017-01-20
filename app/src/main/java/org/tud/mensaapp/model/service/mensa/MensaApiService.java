package org.tud.mensaapp.model.service.mensa;

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
import org.tud.mensaapp.model.entity.Day;
import org.tud.mensaapp.model.entity.Meal;
import org.tud.mensaapp.model.entity.Mensa;
import org.tud.mensaapp.model.service.global.NetworkService;

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

    public void findAllMensasOrderedByDistance(final Context context, Location location, int radius, final FetchMensaListener<List<Mensa>> callback) {
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
                        }
                )
        );
    }

    public void findAllDaysByMensaId(final Context context, final String mensaId, final FetchMensaListener<List<Day>> callback) {
        if (callback != null) {
            callback.onStarted();
        }

        String uri = String.format("http://openmensa.org/api/v2/canteens/%1$s/days", mensaId);
        NetworkService.getInstance(context).addToRequestQueue(
                new JsonArrayRequest(
                        Request.Method.GET,
                        uri,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                List<Day> days = gson.fromJson(
                                        response.toString(),
                                        new TypeToken<List<Day>>() {
                                        }.getType()
                                );
                                if (callback != null) {
                                    callback.onSuccess(true, days);
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
                        }
                )
        );
    }

    public void findAllMeals(final Context context, final String mensaId, final String day, final FetchMensaListener<List<Meal>> callback) {
        if (callback != null) {
            callback.onStarted();
        }

        String uri = String.format("http://openmensa.org/api/v2/canteens/%1$s/days/%2$s/meals", mensaId, day);
        NetworkService.getInstance(context).addToRequestQueue(
                new JsonArrayRequest(
                        Request.Method.GET,
                        uri,
                        null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                List<Meal> meals = gson.fromJson(
                                        response.toString(),
                                        new TypeToken<List<Meal>>() {
                                        }.getType()
                                );
                                if (callback != null) {
                                    callback.onSuccess(true, meals);
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
                        }
                )
        );
    }
}
