package org.tud.mensadresden.finding.offers.view;

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

import com.android.volley.VolleyError;

import org.tud.mensadresden.R;
import org.tud.mensadresden.finding.offers.service.MensaService;
import org.tud.mensadresden.service.LocationService;

import java.util.List;

public class ListMensaFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, MensaService.JobOffersUpdateListener, LocationService.LocationUpdateListener {
    private LocationService locationService;
    private MensaService mensaService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListMensaAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationService = LocationService.getInstance();
        mensaService = MensaService.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.job_offers_list, container, false);
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

        adapter = new ListMensaAdapter();
        adapter.setCurrentLocation(locationService.getMostRecentLocation());
        recyclerView.setAdapter(adapter);

        if (mensaService.getMostRecentlyFetchedJobs() == null) {
            if (locationService.getMostRecentLocation() != null) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        onRefresh();
                    }
                });
            }
        } else {
            adapter.setItems(mensaService.getMostRecentlyFetchedJobs());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        locationService.addLocationUpdateListener(this);
        mensaService.addJobOffersUpdateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        locationService.removeLocationUpdateListener(this);
        mensaService.removeJobOffersUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        MensaService.getInstance(getContext()).refreshJobOffers(getContext(), new MensaService.JobOffersRefreshCallback() {
            @Override
            public void onSuccess() {
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFail(MensaService.JobOffersRefreshError errorStatus, VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                switch (errorStatus) {
                    case LOCATION_UNAVAILABLE:
                        Toast.makeText(getContext(), "Could not refresh: location unavailable", Toast.LENGTH_LONG).show();
                        break;
                    case NETWORK_ERROR:
                        Toast.makeText(getContext(), "Could not refresh: network error", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onLocationUpdate(Location location) {
        adapter.setCurrentLocation(location);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onJobOffersUpdate(List<Job> mostRecentlyFetchedJobs) {
        adapter.setItems(mostRecentlyFetchedJobs);
    }
}
