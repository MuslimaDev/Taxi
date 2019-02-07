package com.example.user.taxi.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.taxi.R;
import com.example.user.taxi.Taxi;
import com.example.user.taxi.models.Company;
import com.example.user.taxi.models.Driver;
import com.example.user.taxi.models.Example;
import com.example.user.taxi.network.RetrofitService;
import com.example.user.taxi.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.InfoWindow;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    private MapView mapView;
    private MapboxMap map;
    com.google.android.gms.maps.model.Marker gmsMarker;
    private Marker marker;
    private RetrofitService retrofitService;
    private double lon, lat;
    private String tel;
    private ImageView myLocation;
    private MarkerOptions markerOptions;
    private LocationManager locationManager;
    private Location location;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        myLocation = findViewById(R.id.myLocationButton);
        initMap(savedInstanceState);
        retrofitService = Taxi.get(getApplicationContext()).getRetrofitService();
        myLocation.setOnClickListener(this);
        if (PermissionUtils.Companion.isLocationEnable(this)) {
            getCurrentLocation();
        }

        getDriversLocation();

    }

    private void initMap(Bundle savedInstanceState) {
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MainActivity.this.map = mapboxMap;
        mapboxMap.setStyle(Style.DARK);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(42.87922298, 74.61795241))
                .title("Current Location"));
    }

    private void getDriversLocation() {
        retrofitService.getDriverLocation(42.8792502, 74.6178904)
                .enqueue(new Callback<Example>() {
                    @SuppressLint("LogNotTimber")
                    @Override
                    public void onResponse(@NotNull Call<Example> call, @NotNull Response<Example> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            for (Company company : response.body().getCompanies()) {
                                for (Driver driver : company.getDrivers()) {
                                    Icon icon = IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.taxi_icon);
                                    LatLng cars = new LatLng(driver.getLat(), driver.getLon());

                                    Log.d("cars", String.valueOf(cars.getLatitude() + cars.getLongitude()));

                                    MarkerOptions markerOpt = new MarkerOptions();
                                    map.addMarker(new MarkerOptions()
                                            .position(cars)
                                            .title("abc")
                                            .icon(icon));

                                    CalloutWindowActivity adapter = new CalloutWindowActivity(MainActivity.this);
                                    map.setInfoWindowAdapter(adapter);
/*
                                    map.addMarker(markerOpt).getInfoWindow();
*/

                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Server is not responding", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<Example> call, @NotNull Throwable t) {
                        Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
            }
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

            public void onSuccess(Location location) {
                if (location != null) {
                    Intent intent = new Intent();
                    intent.putExtra("location1", location.getLatitude());
                    intent.putExtra("location2", location.getLongitude());
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    public void cameraUpdate(double lat, double lng) {
        if (map != null) {
            Log.d("LocCameraUpdate", String.valueOf(lat + " " + lng));
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lat - 0.01, lng))
                    .bearing(0)
                    .zoom(13).tilt(15).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position));


        }

    }

    @Override
    public void onClick(View v) {
        cameraUpdate(42.87922298, 74.61795241);
    }
}