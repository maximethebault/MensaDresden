package org.tud.mensaapp.ui.finding.mensa;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.tud.mensaapp.R;
import org.tud.mensaapp.model.entity.Mensa;
import org.tud.mensaapp.model.service.global.LocationService;
import org.tud.mensaapp.model.service.mensa.ErrorType;
import org.tud.mensaapp.model.service.mensa.FetchMensaListener;
import org.tud.mensaapp.model.service.mensa.MensaService;

import java.util.List;

public class ListMensaFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LocationService.LocationUpdateListener, FetchMensaListener<List<Mensa>> {
    private LocationService locationService;
    private MensaService mensaService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListMensaAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationService = LocationService.getInstance();
        mensaService = MensaService.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mensa_offers_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.job_offers_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.job_offers_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter = new ListMensaAdapter(getContext());
        adapter.setCurrentLocation(locationService.getMostRecentLocation());
        recyclerView.setAdapter(adapter);

        onRefresh();
    }

    @Override
    public void onStart() {
        super.onStart();

        locationService.addLocationUpdateListener(this);
        mensaService.addMensaUpdateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        locationService.removeLocationUpdateListener(this);
        mensaService.removeMensaUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        mensaService.fetchMensa(getContext());
    }

    @Override
    public void onLocationUpdate(Location location) {
        adapter.setCurrentLocation(location);
        adapter.notifyDataSetChanged();
        onRefresh();
    }

    @Override
    public void onStarted() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onSuccess(boolean wasMensaListUpdated, List<Mensa> mensas) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        adapter.setItems(mensas);
    }

    @Override
    public void onFail(ErrorType errorType, String error) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        switch (errorType) {
            case LOCATION_ERROR:
                Toast.makeText(getContext(), "Could not refresh: location unavailable", Toast.LENGTH_LONG).show();
                break;
            case NETWORK_ERROR:
                Toast.makeText(getContext(), "Could not refresh: network error", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
