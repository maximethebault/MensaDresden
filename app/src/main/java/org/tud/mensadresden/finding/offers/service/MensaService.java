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
import org.tud.mensadresden.service.LocationService;
import org.tud.mensadresden.service.NetworkService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensaService {

    public interface JobOffersUpdateListener {
        void onJobOffersUpdate(List<Job> mostRecentlyFetchedJobs);
    }

    public interface JobOffersRefreshCallback {
        void onSuccess();

        void onFail(JobOffersRefreshError errorStatus, VolleyError error);
    }

    public enum JobOffersRefreshError {
        LOCATION_UNAVAILABLE,
        NETWORK_ERROR
    }

    private static MensaService mInstance;
    private List<MensaService.JobOffersUpdateListener> listeners;
    private Gson gson;
    private List<Job> mostRecentlyFetchedJobs;
    private Map<String, Job> idToJob;

    private MensaService() {
        listeners = new ArrayList<>();
        gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationDeserializer())
                .excludeFieldsWithModifiers(Modifier.STATIC)// override default so that transient fields are still taken into account
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        idToJob = new HashMap<>();
    }

    public static synchronized MensaService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MensaService();
            mInstance.readJobsFromCache(context);
        }
        return mInstance;
    }

    public void refreshJobOffers(final Context context, final JobOffersRefreshCallback callback) {
        LocationService locationService = LocationService.getInstance();
        Location lastLocation = locationService.getMostRecentLocation();
        if (lastLocation == null) {
            locationService.requestLocationUpdates();
            if (callback != null) {
                callback.onFail(JobOffersRefreshError.LOCATION_UNAVAILABLE, null);
            }
            return;
        }
        String uri = String.format(
                "http://94.23.120.133:3460/joboffers?lat=%1$s&long=%2$s&range=%3$s",
                lastLocation.getLatitude(),
                lastLocation.getLongitude(),
                5000000
        );
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
                                for (JobOffersUpdateListener listener : listeners) {
                                    listener.onJobOffersUpdate(mostRecentlyFetchedJobs);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (callback != null) {
                            callback.onFail(JobOffersRefreshError.NETWORK_ERROR, error);
                        }
                    }
                }));
    }

    private void buildJobMap() {
        idToJob.clear();
        if (mostRecentlyFetchedJobs == null) {
            return;
        }
        for (Job job : mostRecentlyFetchedJobs) {
            idToJob.put(job.getId(), job);
        }
    }

    private void writeJobsToCache(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("job_cache", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(mostRecentlyFetchedJobs);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJobsFromCache(Context context) {
        try {
            FileInputStream fis = context.openFileInput("job_cache");
            ObjectInputStream ois = new ObjectInputStream((new BufferedInputStream(fis)));
            mostRecentlyFetchedJobs = (List<Job>) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        buildJobMap();
    }

    public void addJobOffersUpdateListener(JobOffersUpdateListener jobOffersUpdateListener) {
        listeners.add(jobOffersUpdateListener);
    }

    public void removeJobOffersUpdateListener(JobOffersUpdateListener jobOffersUpdateListener) {
        listeners.remove(jobOffersUpdateListener);
    }

    public List<Job> getMostRecentlyFetchedJobs() {
        return mostRecentlyFetchedJobs;
    }

    public Map<String, Job> getIdToJob() {
        return idToJob;
    }
}
