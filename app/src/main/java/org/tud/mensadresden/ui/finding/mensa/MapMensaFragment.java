package org.tud.mensadresden.ui.finding.mensa;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
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
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
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
import org.tud.mensadresden.model.entity.Day;
import org.tud.mensadresden.model.entity.Mensa;
import org.tud.mensadresden.model.service.global.LocationService;
import org.tud.mensadresden.model.service.mensa.ErrorType;
import org.tud.mensadresden.model.service.mensa.FetchMensaListener;
import org.tud.mensadresden.model.service.mensa.MensaService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static org.tud.mensadresden.model.service.mensa.Utils.formatDistance;

public class MapMensaFragment extends Fragment implements LocationService.LocationUpdateListener, FetchMensaListener<List<Mensa>> {
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

    private String lastClickedMensaId;

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
        return inflater.inflate(R.layout.mensa_offers_map, container, false);
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

        bottomSheetFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFABClick();
            }
        });

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
                        lastClickedMensaId = marker.getTitle();
                        updateBottomSheetWithJob(lastClickedMensaId);
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
            bottomSheetLoader.setVisibility(View.VISIBLE);
            bottomSheetDescription.setText("");
            mensaService.fetchDaysForMensaId(getContext(), mensaId, new FetchMensaListener<List<Day>>() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onSuccess(boolean wasMensaListUpdated, final List<Day> results) {
                    final Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetLoader.setVisibility(View.GONE);
                            bottomSheetDescription.setText(daysToString(results));
                        }
                    });
                }

                @Override
                public void onFail(ErrorType errorType, String error) {
                    final Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetLoader.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    private Spanned daysToString(List<Day> results) {
        List<String> openingDays = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesertday = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formattingSdf = new SimpleDateFormat("EEEE dd/MM/yy", Locale.getDefault());
        for (Day day : results) {
            try {
                Date date = sdf.parse(day.getDate());
                if (date.before(yesertday)) {
                    continue;
                }
                openingDays.add("<b>" + formattingSdf.format(date) + "</b><br>" + (day.isClosed() ? "<font color=\"#FF0000\">closed</font>" : "<font color=\"#00FF00\">open</font>"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(TextUtils.join("<br>", openingDays), FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(TextUtils.join("<br>", openingDays));
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

    private void onFABClick() {
        Intent i = new Intent(getContext(), MensaDetailsActivity.class);
        i.putExtra("id", lastClickedMensaId);
        startActivity(i);
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
