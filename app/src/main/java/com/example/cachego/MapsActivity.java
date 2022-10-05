package com.example.cachego;

import static com.example.cachego.R.color.green;
import static com.example.cachego.R.color.yellow;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cachego.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    //Location Tracking
    LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 50; // get GPS location every 1 min
    private static final int GPS_DISTANCE = 1000; // set distance value in meter
    private static final int HANDLER_DELAY = 50;
    private static final int START_HANDLER_DELAY = 0;
    final static String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSION_ALL = 1;
    private Marker myLocation;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    Fragment OverlayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        OverlayFragment = new fragment_account();
        transaction.replace(R.id.overlay_fragment, OverlayFragment);
        transaction.commit();*/

        SetOverlayFragment(1);

        //Permissions for newer SDK
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSION, PERMISSION_ALL);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Location Updating
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run()
            {
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);
        
        //Nav Bar Button CLicks
        //ImageButton Index 0
        binding.navAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });

        //ImageButton Index 1
        binding.navMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 1;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });

        //ImageButton Index 2
        binding.navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 2;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });
    }

    //Set Active NavBar Color
    private void SetActiveNavBarColor(int index) {
        binding.navAccount.setBackgroundColor(getResources().getColor(yellow));
        binding.navMap.setBackgroundColor(getResources().getColor(yellow));
        binding.navSettings.setBackgroundColor(getResources().getColor(yellow));

        switch (index) {
            case 0:
                binding.navAccount.setBackgroundColor(getResources().getColor(green));
                break;
            case 1:
                binding.navMap.setBackgroundColor(getResources().getColor(green));
                break;
            case 2:
                binding.navSettings.setBackgroundColor(getResources().getColor(green));
                break;
        }
    }

    //Set OverlayFragment
    private void SetOverlayFragment(int index) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (index) {
            case 0:
                OverlayFragment = new fragment_account();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
                break;
            case 1:
                OverlayFragment = new fragment_account();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.hide(OverlayFragment);
                transaction.commit();
                break;
        }
    }

    /*
    weatherFragment = new WeatherFragment();
    tideFragment = new TideFragment();

    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();
    transaction.replace(R.id.weather_fragment_container, weatherFragment);
    transaction.replace(R.id.tide_fragment_container, tideFragment);
    transaction.commit();
    */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        try {
            myLocation.remove();
        }
        catch (Exception e) {

        }
        Log.d("mylog", "Got Location: " + location.getLatitude() + ", "
                + location.getLongitude());
        locationManager.removeUpdates(this);

        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        myLocation = mMap.addMarker(new MarkerOptions().position(loc).title("Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, this);
            }
        }
    }
}