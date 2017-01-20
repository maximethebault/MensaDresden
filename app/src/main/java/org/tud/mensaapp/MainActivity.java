package org.tud.mensaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.tud.mensaapp.model.service.global.LocationService;
import org.tud.mensaapp.ui.dialog.ConnectionFailedDialog;
import org.tud.mensaapp.ui.finding.AboutFragment;
import org.tud.mensaapp.ui.finding.mensa.TabMensaFragment;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationService.LocationRequestListener {

    private enum ItemLabel {
        MENSA_LIST(2),
        ABOUT(7);

        public final long id;

        ItemLabel(int id) {
            this.id = id;
        }

        public static ItemLabel findByid(long id) {
            for (ItemLabel il : values()) {
                if (il.id == id) {
                    return il;
                }
            }
            return null;
        }
    }

    private HashMap<ItemLabel, Class<? extends Fragment>> itemToFragment;
    private HashMap<ItemLabel, Float> itemToToolbarElevation;

    private Drawer drawer;

    private Menu menu;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private boolean isReceivingLocationUpdates;
    private LocationService locationService;

    final static int REQUEST_LOCATION = 199;
    final static int REQUEST_LOCATION_PERMISSION = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemToFragment = new HashMap<>();
        itemToFragment.put(ItemLabel.MENSA_LIST, TabMensaFragment.class);
        itemToFragment.put(ItemLabel.ABOUT, AboutFragment.class);

        itemToToolbarElevation = new HashMap<>();
        itemToToolbarElevation.put(ItemLabel.MENSA_LIST, 0f);
        itemToToolbarElevation.put(ItemLabel.ABOUT, 6f);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        setupDrawer(toolbar, savedInstanceState);

        setupLocationService();

        isReceivingLocationUpdates = false;
        locationService = LocationService.getInstance();
        locationService.setLocationRequestListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    private void setupDrawer(final Toolbar toolbar, Bundle savedInstanceState) {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        //new PrimaryDrawerItem().withName("Home").withIdentifier(ItemLabel.HOME.id).withIcon(GoogleMaterial.Icon.gmd_home),

                        new SectionDrawerItem().withName(R.string.drawer_mensa).withDivider(false),
                        new PrimaryDrawerItem().withName(R.string.drawer_offers).withIdentifier(ItemLabel.MENSA_LIST.id).withIcon(FontAwesome.Icon.faw_location_arrow)
                )
                .withSelectedItem(ItemLabel.MENSA_LIST.id)
                .withFireOnInitialOnClick(true)
                .addStickyDrawerItems(
                        new PrimaryDrawerItem().withName("About").withIdentifier(ItemLabel.ABOUT.id).withIcon(FontAwesome.Icon.faw_question)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem == null) {
                            return true;
                        }
                        ItemLabel itemToLoad = ItemLabel.findByid(drawerItem.getIdentifier());
                        if (itemToFragment.containsKey(itemToLoad)) {
                            toolbar.setElevation(itemToToolbarElevation.get(itemToLoad));

                            final Class<? extends Fragment> fragmentClassToLoad = itemToFragment.get(itemToLoad);
                            try {
                                FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
                                Fragment fragmentToReplace = fragmentManager.findFragmentById(R.id.activity_main_content);
                                if (fragmentToReplace != null && fragmentToReplace.getClass().equals(fragmentClassToLoad)) {
                                    return false;
                                }
                                FragmentTransaction transaction = fragmentManager.beginTransaction()
                                        .replace(R.id.activity_main_content, fragmentClassToLoad.newInstance());
                                if (fragmentToReplace != null) {
                                    // only add it to the fragment stack if it's actually replacing another fragment
                                    transaction.addToBackStack(null);
                                }
                                transaction.commit();
                            } catch (InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                        return true;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        // disable scrollbar on the drawer
        drawer.getRecyclerView().setVerticalScrollBarEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        drawer.saveInstanceState(outState);
    }

    private void setupLocationService() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(1000);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermissionAndRequestLocationUpdates();
    }

    @Override
    public void onLocationRequest() {
        if (isReceivingLocationUpdates) {
            return;
        }
        ((MensaApplication) getApplication()).setHasReceivedLocationPermissionPrompt(false);
        ((MensaApplication) getApplication()).setHasReceivedLocationEnablePrompt(false);
        if (googleApiClient.isConnected()) {
            checkPermissionAndRequestLocationUpdates();
        }
    }

    private void checkPermissionAndRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (((MensaApplication) getApplication()).hasReceivedLocationPermissionPrompt()) {
                // if the activity has been re-created after a configuration change, we don't prompt the user again. We don't wanna be annoying.
                return;
            }
            ((MensaApplication) getApplication()).setHasReceivedLocationPermissionPrompt(true);
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        requestLocationUpdates();
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                boolean authorized = false;
                for (int grantResult : grantResults) {
                    authorized = authorized || grantResult == PackageManager.PERMISSION_GRANTED;
                }
                if (authorized) {
                    // permission was granted
                    requestLocationUpdates();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show permission explanation dialog...
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                    }
                }
                return;
            }
        }
    }

    private void requestLocationUpdates() {
        locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)// shows a dialog asking the user to enable the GPS
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            @SuppressWarnings("MissingPermission")
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        LocationService.getInstance().setMostRecentLocation(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);
                        isReceivingLocationUpdates = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        if (((MensaApplication) getApplication()).hasReceivedLocationEnablePrompt()) {
                            // if the activity has been re-created after a configuration change, we don't prompt the user again. We don't wanna be annoying.
                            return;
                        }
                        ((MensaApplication) getApplication()).setHasReceivedLocationEnablePrompt(true);
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_main_layout), "Lost connection to Google Play Services", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        ConnectionFailedDialog connectionFailedDialog = new ConnectionFailedDialog();
        connectionFailedDialog.setCancelable(false);
        connectionFailedDialog.show(getSupportFragmentManager(), "connectionFailedDialog");
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        LocationService.getInstance().setMostRecentLocation(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MainActivity.this);
                        isReceivingLocationUpdates = true;
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationService.getInstance().setMostRecentLocation(location);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.setLocationRequestListener(null);
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, MainActivity.this);
        }
    }
}
