package com.example.myapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentMarker;
    private LatLng selectedLocation;
    private TextView tvSelectedAddress;
    private Button btnConfirmLocation;
    private Button btnUseCurrentLocation;

    private String selectedAddress = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                resultIntent.putExtra("address", selectedAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        btnUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Check location permission
        if (checkLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }

        // Default location (Melbourne)
        LatLng defaultLocation = new LatLng(-37.8136, 144.9631);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableMyLocation() {
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        onMapClick(currentLatLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    } else {
                        Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        selectedLocation = latLng;

        // Update marker
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        // Get address from coordinates
        getAddressFromLatLng(latLng.latitude, latLng.longitude);
    }

    private void getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedAddress = addresses.get(0).getAddressLine(0);
                tvSelectedAddress.setText("Selected: " + selectedAddress);
            } else {
                selectedAddress = latitude + ", " + longitude;
                tvSelectedAddress.setText("Selected: " + selectedAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
            selectedAddress = latitude + ", " + longitude;
            tvSelectedAddress.setText("Selected: " + selectedAddress);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}