package com.example.cachego;

import static com.example.cachego.R.color.blue;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cachego.databinding.ActivityMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    private LatLng loc;

    //Location found
    private boolean locFound = false;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseAuth FireAuth;

    private ArrayList<Cache> cacheArrayList;

    Fragment OverlayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SetOverlayFragment(1);

        //Permissions for newer SDK
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSION, PERMISSION_ALL);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Location Updating
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
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

        binding.btnRelocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                } catch (Exception e) {

                }
            }
        });

        LoadCaches();
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
                binding.navMap.setBackgroundColor(getResources().getColor(blue));
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
            case 2:
                OverlayFragment = new SettingsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
                break;
        }
    }

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
    public void onLocationChanged(@NonNull Location location) {
        try {
            myLocation.remove();
        } catch (Exception e) {

        }
        Log.d("mylog", "Got Location: " + location.getLatitude() + ", "
                + location.getLongitude());
        locationManager.removeUpdates(this);

        loc = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(loc);

        //Icon Creation
        int height = 50;
        int width = 50;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_user_icon);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        //Set Marker Icon
        marker.icon(smallMarkerIcon);
        myLocation = mMap.addMarker(marker);

        if (locFound == false) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            locFound = true;
        }
    }

    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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

    private void LoadCaches() {
        cacheArrayList = new ArrayList<>();
        DatabaseReference refCollections = FirebaseDatabase.getInstance().getReference("caches");

        refCollections.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cacheArrayList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Cache c = snap.getValue(Cache.class);

                    cacheArrayList.add(c);
                    AddCacheToMap(c);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void AddCacheToMap(Cache c) {
        //Icon size
        int height = 100;
        int width = 100;

        //Easy Default Icon
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_easy);

        //Marker Object Creation
        LatLng cache_location = new LatLng(c.getLat(), c.getLon());
        MarkerOptions marker = new MarkerOptions().position(cache_location);

        //Check Difficulty
        switch (c.getDifficulty()) {
            case "easy":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_easy);
                break;
            case "normal":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_normal);
                break;
            case "hard":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_hard);
                break;
        }

        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        marker.icon(smallMarkerIcon);
        Marker cache_marker = mMap.addMarker(marker);
    }
}