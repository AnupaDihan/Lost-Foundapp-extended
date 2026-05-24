package com.example.myapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.DatabaseHelper;
import com.example.myapplication.models.Item;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private GoogleMap mMap;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Item> allItems;
    private Location userLocation;

    private TextInputEditText editRadius;
    private MaterialButton buttonApplyFilter;
    private MaterialButton buttonResetFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editRadius = findViewById(R.id.editRadius);
        buttonApplyFilter = findViewById(R.id.buttonApplyFilter);
        buttonResetFilter = findViewById(R.id.buttonResetFilter);

        setupToolbar();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonApplyFilter.setOnClickListener(v -> applyRadiusFilter());
        buttonResetFilter.setOnClickListener(v -> resetFilter());

        findViewById(R.id.fabMyLocation).setOnClickListener(v -> {
            if (userLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 14));
            } else {
                getCurrentLocation();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We use our own FAB
        mMap.setOnInfoWindowClickListener(this);

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Show all items initially
        loadAllMarkers();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) return;

        Log.d(TAG, "Fetching current location...");
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = location;
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Current location found: " + userLatLng.toString());
                        // Only move camera if we're not currently showing a radius filter
                        if (editRadius.getText() == null || editRadius.getText().toString().isEmpty()) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                        }
                    } else {
                        Log.w(TAG, "Current location is null");
                        // Fallback to last known location if getCurrentLocation fails
                        fusedLocationClient.getLastLocation().addOnSuccessListener(this, lastLoc -> {
                            if (lastKnownLocationAvailable(lastLoc)) {
                                userLocation = lastLoc;
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting location: " + e.getMessage()));
    }

    private boolean lastKnownLocationAvailable(Location location) {
        if (location != null) {
            userLocation = location;
            return true;
        }
        return false;
    }

    private void loadAllMarkers() {
        if (mMap == null) return;
        mMap.clear();

        allItems = databaseHelper.getAllItems();
        Log.d(TAG, "Loading markers for " + allItems.size() + " items");

        if (allItems.isEmpty()) {
            Toast.makeText(this, "No items found to show on map", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
        int markerCount = 0;
        for (Item item : allItems) {
            if (item.getLatitude() != 0 || item.getLongitude() != 0) {
                LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
                addMarkerForItem(item);
                builder.include(position);
                markerCount++;
            }
        }
        
        Log.d(TAG, "Added " + markerCount + " markers to map");
        
        if (markerCount > 0) {
            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (IllegalStateException e) {
                // Bounds might fail if map layout not finished, fallback to zoom on first marker
                if (!allItems.isEmpty()) {
                    Item first = allItems.get(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(first.getLatitude(), first.getLongitude()), 10));
                }
            }
        } else {
            Toast.makeText(this, "Items found but they don't have valid coordinates", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarkerForItem(Item item) {
        if (item.getLatitude() == 0 && item.getLongitude() == 0) return;

        LatLng position = new LatLng(item.getLatitude(), item.getLongitude());
        
        // Determine color: Red for Lost, Green for Found
        float markerColor = (item.getType() != null && item.getType().equalsIgnoreCase("lost")) ?
                BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN;

        // Create the marker with the item name tag
        com.google.android.gms.maps.model.Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(item.getTitle()) // Item name as the primary tag
                .snippet(item.getType().toUpperCase() + " at " + item.getLocation())
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        
        if (marker != null) {
            marker.setTag(item.getId());
        }
    }

    private void applyRadiusFilter() {
        if (editRadius.getText() == null) return;
        String radiusStr = editRadius.getText().toString().trim();
        if (radiusStr.isEmpty()) {
            Toast.makeText(this, "Please enter a radius", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userLocation == null) {
            Toast.makeText(this, "Could not determine your location. Please ensure GPS is on.", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        try {
            double radiusKm = Double.parseDouble(radiusStr);
            double radiusInMeters = radiusKm * 1000;
            mMap.clear();

            // Always refresh allItems from DB before filtering
            allItems = databaseHelper.getAllItems();

            // Visualize the radius on the map
            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            mMap.addCircle(new CircleOptions()
                    .center(userLatLng)
                    .radius(radiusInMeters)
                    .strokeColor(0x550000FF)
                    .fillColor(0x220000FF)
                    .strokeWidth(2));

            List<Item> filteredItemsList = new ArrayList<>();
            for (Item item : allItems) {
                if (item.getLatitude() == 0 && item.getLongitude() == 0) continue;

                float[] results = new float[1];
                Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                        item.getLatitude(), item.getLongitude(), results);
                
                float distanceInMeters = results[0];

                if (distanceInMeters <= radiusInMeters) {
                    filteredItemsList.add(item);
                    addMarkerForItem(item);
                }
            }

            // Adjust camera to focus on the search area
            float zoomLevel = getZoomLevel(radiusInMeters);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel));

            Toast.makeText(this, "Found " + filteredItemsList.size() + " items within " + radiusKm + "km", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid radius value", Toast.LENGTH_SHORT).show();
        }
    }

    private float getZoomLevel(double radiusInMeters) {
        double scale = radiusInMeters / 500;
        return (float) (15 - Math.log(scale) / Math.log(2));
    }

    private void resetFilter() {
        editRadius.setText("");
        loadAllMarkers();
    }

    @Override
    public void onInfoWindowClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
        if (marker.getTag() != null) {
            long itemId = (long) marker.getTag();
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("item_id", itemId);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            }
        }
    }
}
