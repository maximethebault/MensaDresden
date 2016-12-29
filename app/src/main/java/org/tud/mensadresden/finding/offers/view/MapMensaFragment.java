package org.tud.mensadresden.finding.offers.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.tud.mensadresden.R;
import org.tud.mensadresden.finding.offers.model.Mensa;
import org.tud.mensadresden.finding.offers.service.ErrorType;
import org.tud.mensadresden.finding.offers.service.FetchMensaListener;
import org.tud.mensadresden.finding.offers.service.MensaService;
import org.tud.mensadresden.service.LocationService;

import java.util.List;

import static org.tud.mensadresden.finding.offers.service.Utils.formatDistance;

public class MapMensaFragment extends Fragment implements LocationService.LocationUpdateListener, FetchMensaListener {
    private LocationService locationService;
    private MensaService mensaService;
    private SupportMapFragment fragment;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private CoordinatorLayout coordinatorLayout;
    private View bottomSheetView;
    private LocationSource.OnLocationChangedListener mapLocationProviderCallback;
    private boolean hasUserInteracted;

    private TextView bottomSheetTitle;
    private TextView bottomSheetEmployer;
    private TextView bottomSheetDistance;
    private TextView bottomSheetDescription;
    private ProgressBar bottomSheetLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationService = LocationService.getInstance();
        mensaService = MensaService.getInstance();

        hasUserInteracted = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.job_offers_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.bottom_sheet_coordinator);
        final LinearLayout bottomSheetTitleContainer = (LinearLayout) view.findViewById(R.id.bottom_sheet_title_container);
        bottomSheetTitle = (TextView) view.findViewById(R.id.bottom_sheet_title);
        bottomSheetEmployer = (TextView) view.findViewById(R.id.bottom_sheet_employer);
        bottomSheetDistance = (TextView) view.findViewById(R.id.bottom_sheet_distance);
        bottomSheetDescription = (TextView) view.findViewById(R.id.bottom_sheet_description);
        bottomSheetLoader = (ProgressBar) view.findViewById(R.id.bottom_sheet_loader);
        final FloatingActionButton bottomSheetFab = (FloatingActionButton) view.findViewById(R.id.bottom_sheet_fab);

        bottomSheetView = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        if (savedInstanceState != null && savedInstanceState.getParcelable("bottom_sheet") != null) {
            bottomSheetBehavior.onRestoreInstanceState(coordinatorLayout, bottomSheetView, savedInstanceState.getParcelable("bottom_sheet"));
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        int colorWhite = Color.WHITE;
        int colorBlue = ContextCompat.getColor(getContext(), R.color.materialBlue);
        int colorBlack = Color.BLACK;
        int colorGrey = Color.argb(255, 85, 85, 85);
        final ValueAnimator colorAnimationWhiteToBlue = ValueAnimator.ofObject(new ArgbEvaluator(), colorWhite, colorBlue);
        colorAnimationWhiteToBlue.setDuration(250); // milliseconds
        colorAnimationWhiteToBlue.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                bottomSheetTitleContainer.setBackgroundColor((int) animator.getAnimatedValue());
                bottomSheetFab.setColorFilter((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationBlueToWhite = ValueAnimator.ofObject(new ArgbEvaluator(), colorBlue, colorWhite);
        colorAnimationBlueToWhite.setDuration(250); // milliseconds
        colorAnimationBlueToWhite.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                bottomSheetFab.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue()));
                bottomSheetDistance.setTextColor(ColorStateList.valueOf((int) animator.getAnimatedValue()));
            }

        });
        final ValueAnimator colorAnimationBlackToWhite = ValueAnimator.ofObject(new ArgbEvaluator(), colorBlack, colorWhite);
        colorAnimationBlackToWhite.setDuration(250); // milliseconds
        colorAnimationBlackToWhite.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                bottomSheetTitle.setTextColor((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationGreyToWhite = ValueAnimator.ofObject(new ArgbEvaluator(), colorGrey, colorWhite);
        colorAnimationGreyToWhite.setDuration(250); // milliseconds
        colorAnimationGreyToWhite.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                bottomSheetEmployer.setTextColor((int) animator.getAnimatedValue());
            }

        });
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            colorAnimationWhiteToBlue.end();
            colorAnimationBlueToWhite.end();
            colorAnimationBlackToWhite.end();
            colorAnimationGreyToWhite.end();
        }
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private boolean isReverse = (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED);

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetLoader.setVisibility(View.GONE);
                        }
                    }, 1000);
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetLoader.setVisibility(View.VISIBLE);
                        }
                    }, 0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                if (slideOffset > 0) {
                    if (isReverse && !colorAnimationWhiteToBlue.isStarted()) {
                        isReverse = false;
                        // animation when the bottom sheet goes from collapsed -> expanded
                        colorAnimationWhiteToBlue.start();
                        colorAnimationBlueToWhite.start();
                        colorAnimationBlackToWhite.start();
                        colorAnimationGreyToWhite.start();
                    }
                } else {
                    if (!isReverse && !colorAnimationWhiteToBlue.isStarted()) {
                        isReverse = true;
                        // animation when the bottom sheet goes from expanded -> collapsed
                        colorAnimationWhiteToBlue.reverse();
                        colorAnimationBlueToWhite.reverse();
                        colorAnimationBlackToWhite.reverse();
                        colorAnimationGreyToWhite.reverse();
                    }
                }
            }
        });
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bottomSheetBehavior != null) {
            outState.putParcelable("bottom_sheet", bottomSheetBehavior.onSaveInstanceState(coordinatorLayout, bottomSheetView));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.job_offers_map_container);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fragment.setRetainInstance(true);
            fm.beginTransaction().replace(R.id.job_offers_map_container, fragment).commit();
            setupMap();
        }
        updateMap();
    }

    private void setupMap() {
        fragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressWarnings("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setLocationSource(new LocationSource() {
                    @Override
                    public void activate(OnLocationChangedListener onLocationChangedListener) {
                        LocationService locationService = LocationService.getInstance();
                        if (locationService.getMostRecentLocation() != null) {
                            onLocationChangedListener.onLocationChanged(locationService.getMostRecentLocation());
                        }
                        mapLocationProviderCallback = onLocationChangedListener;
                    }

                    @Override
                    public void deactivate() {
                        mapLocationProviderCallback = null;
                    }
                });
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        hasUserInteracted = true;
                    }
                });

                Location mostRecentLocation = locationService.getMostRecentLocation();
                if (mostRecentLocation != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude()), 17.0f));
                }
            }
        });
    }

    private void updateMap() {
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String mensaId = marker.getTitle();
                        updateBottomSheetWithJob(mensaId);
                        // show the bottom sheet
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        // center the camero on the marker
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);

                        // return true to prevent default action (show info window)
                        return true;
                    }
                });
            }
        });
    }

    private void updateBottomSheetWithJob(String mensaId) {
        Mensa mensa = mensaService.getMensaById(getContext(), mensaId);
        if (mensa != null) {
            bottomSheetTitle.setText(mensa.getName());
            bottomSheetEmployer.setText(mensa.getAddress());
            bottomSheetDescription.setText(mensa.getAddress());
            Location currentLocation = LocationService.getInstance().getMostRecentLocation();
            if (currentLocation != null) {
                bottomSheetDistance.setText(formatDistance(mensa.getCoordinates().distanceTo(currentLocation), 0));
            } else {
                bottomSheetDistance.setText("");
            }
        }
    }

    private void updateMapMarker(final List<Mensa> mensas) {
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (getContext() == null) {
                    return;
                }
                googleMap.clear();
                for (Mensa mensa : mensas) {
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .position(new LatLng(mensa.getCoordinates().getLatitude(), mensa.getCoordinates().getLongitude()))
                            .title(String.valueOf(mensa.getId())));
                }
            }
        });
    }

    @Override
    public void onLocationUpdate(final Location location) {
        if (mapLocationProviderCallback != null) {
            mapLocationProviderCallback.onLocationChanged(location);

            if (!hasUserInteracted) {
                fragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
                    }
                });
            }
        }
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onSuccess(boolean wasMensaListUpdated, List<Mensa> mensas) {
        updateMapMarker(mensas);
    }

    @Override
    public void onFail(ErrorType errorType, String error) {

    }
}
