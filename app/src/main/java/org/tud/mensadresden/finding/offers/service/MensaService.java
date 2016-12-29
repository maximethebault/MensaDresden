package org.tud.mensadresden.finding.offers.service;

import android.content.Context;
import android.location.Location;

import org.tud.mensadresden.finding.offers.model.Mensa;
import org.tud.mensadresden.finding.offers.repository.SqlMensaRepository;
import org.tud.mensadresden.service.LocationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MensaService {
    private static MensaService mInstance;
    private static int FETCH_THROTTLE_INTERVAL = 5000;

    private AtomicBoolean isAlreadyFetching;
    private long lastFetchTime;
    private List<Mensa> lastFetchData;

    private List<FetchMensaListener> fetchMensaListeners;

    private MensaApiService mensaApiService;

    private MensaService() {
        isAlreadyFetching = new AtomicBoolean(false);
        lastFetchTime = 0;
        lastFetchData = null;

        fetchMensaListeners = new ArrayList<>();

        mensaApiService = MensaApiService.getInstance();
    }

    public static synchronized MensaService getInstance() {
        if (mInstance == null) {
            mInstance = new MensaService();
        }
        return mInstance;
    }

    public void fetchMensa(final Context context) {
        // try to fetch Mensa from API
        // => success: store in database, callback, call update fetchMensaListeners
        // => fail: retrieve from database. if nothing exists, callback with error returned by API
        if (!isAlreadyFetching.compareAndSet(false, true)) {
            return;
        }
        if (System.currentTimeMillis() - lastFetchTime <= FETCH_THROTTLE_INTERVAL) {
            triggerSuccessListeners(false, lastFetchData);
        }

        triggerStartListeners();

        LocationService locationService = LocationService.getInstance();
        final Location lastLocation = locationService.getMostRecentLocation();
        if (lastLocation == null) {
            locationService.requestLocationUpdates();
        }

        final SqlMensaRepository mensaRepository = SqlMensaRepository.getInstance(context);

        mensaApiService.findAllOrderedByDistance(context, lastLocation, 5000000, new FetchMensaListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onSuccess(boolean wasMensaListUpdated, List<Mensa> mensas) {
                mensaRepository.deleteAll();
                mensaRepository.insertAll(mensas);
                triggerSuccessListeners(true, mensas);
                lastFetchTime = System.currentTimeMillis();
                lastFetchData = mensas;
                isAlreadyFetching.set(false);
            }

            @Override
            public void onFail(ErrorType errorType, String error) {
                List<Mensa> mensas = mensaRepository.findAllOrderedByDistance(lastLocation);
                if (mensas == null || mensas.size() == 0) {
                    triggerFailListeners(ErrorType.LOCATION_ERROR, null);
                } else {
                    triggerSuccessListeners(false, mensas);
                }
                isAlreadyFetching.set(false);
            }
        });
    }

    public Mensa getMensaById(final Context context, String id) {
        final SqlMensaRepository mensaRepository = SqlMensaRepository.getInstance(context);
        return mensaRepository.findById(id);
    }

    public void addMensaUpdateListener(FetchMensaListener updateListener) {
        fetchMensaListeners.add(updateListener);
    }

    public void removeMensaUpdateListener(FetchMensaListener updateListener) {
        fetchMensaListeners.remove(updateListener);
    }

    private void triggerStartListeners() {
        for (FetchMensaListener listener : fetchMensaListeners) {
            listener.onStarted();
        }
    }

    private void triggerSuccessListeners(boolean wasMensaListUpdated, List<Mensa> mensas) {
        for (FetchMensaListener listener : fetchMensaListeners) {
            listener.onSuccess(wasMensaListUpdated, mensas);
        }
    }

    private void triggerFailListeners(ErrorType errorType, String error) {
        for (FetchMensaListener listener : fetchMensaListeners) {
            listener.onFail(errorType, error);
        }
    }
}
