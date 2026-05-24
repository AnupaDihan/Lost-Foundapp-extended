package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.DatabaseHelper;
import com.example.myapplication.models.Item;
import com.example.myapplication.utils.ImageUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = "PostActivity";

    private DatabaseHelper databaseHelper;
    private Uri selectedImageUri = null;

    // Location coordinates
    private double selectedLatitude = 0;
    private double selectedLongitude = 0;
    private String selectedLocationName = "";

    // UI Components
    private RadioGroup radioGroupType;
    private RadioButton radioLost;
    private RadioButton radioFound;
    private TextInputEditText editName;
    private TextInputEditText editPhone;
    private TextInputEditText editDescription;
    private TextInputEditText editDate;
    private TextInputEditText editLocation;
    private Spinner spinnerCategory;
    private ImageView imagePreview;
    private MaterialButton buttonSelectImage;
    private MaterialButton buttonSave;
    private MaterialButton buttonGetCurrentLocation;
    private AutocompleteSupportFragment autocompleteFragment;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .centerCrop()
                            .into(imagePreview);
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        databaseHelper = new DatabaseHelper(this);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        initializeViews();
        setupToolbar();
        setupDatePicker();
        setupCategorySpinner();
        setupImagePicker();
        setupLocationAutocomplete();
        setupCurrentLocationButton();
        setupSaveButton();
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

    private void initializeViews() {
        radioGroupType = findViewById(R.id.radioGroupType);
        radioLost = findViewById(R.id.radioLost);
        radioFound = findViewById(R.id.radioFound);
        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editDescription = findViewById(R.id.editDescription);
        editDate = findViewById(R.id.editDate);
        editLocation = findViewById(R.id.editLocation);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imagePreview = findViewById(R.id.imagePreview);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSave = findViewById(R.id.buttonSave);
        buttonGetCurrentLocation = findViewById(R.id.buttonGetCurrentLocation);
    }

    private void setupDatePicker() {
        editDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupCategorySpinner() {
        String[] categories = {"Electronics", "Pets", "Wallets", "Documents", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupImagePicker() {
        buttonSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupLocationAutocomplete() {
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
            ));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    selectedLocationName = place.getName();
                    if (place.getLatLng() != null) {
                        selectedLatitude = place.getLatLng().latitude;
                        selectedLongitude = place.getLatLng().longitude;
                    }
                    editLocation.setText(selectedLocationName);
                    Log.d(TAG, "Place selected: " + selectedLocationName + " at " + selectedLatitude + ", " + selectedLongitude);
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.e(TAG, "Place selection error: " + status.getStatusMessage());
                    Toast.makeText(PostActivity.this, "Error selecting location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupCurrentLocationButton() {
        buttonGetCurrentLocation.setOnClickListener(v -> {
            // Launch LocationPickerActivity or use GPS
            Intent intent = new Intent(PostActivity.this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> locationPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                    selectedLongitude = result.getData().getDoubleExtra("longitude", 0);
                    selectedLocationName = result.getData().getStringExtra("address");
                    editLocation.setText(selectedLocationName);

                    // Get location name from coordinates
                    getAddressFromLatLng(selectedLatitude, selectedLongitude);
                }
            }
    );

    private void getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                selectedLocationName = address;
                editLocation.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSaveButton() {
        buttonSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveItem();
            }
        });
    }

    private boolean validateInputs() {
        Log.d(TAG, "Validating inputs...");
        if (!radioLost.isChecked() && !radioFound.isChecked()) {
            Toast.makeText(this, "Please select Lost or Found", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isFieldValid(editName, "name")) return false;
        if (!isFieldValid(editPhone, "phone number")) return false;
        if (!isFieldValid(editDescription, "description")) return false;
        if (!isFieldValid(editDate, "date")) return false;
        if (!isFieldValid(editLocation, "location")) return false;

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Validation failed: Image not selected");
            return false;
        }

        // If coordinates are missing, try to geocode the manual input
        if (selectedLatitude == 0 && selectedLongitude == 0) {
            String manualLocation = editLocation.getText().toString().trim();
            Log.d(TAG, "Coordinates missing, attempting to geocode: " + manualLocation);
            if (!manualLocation.isEmpty()) {
                geocodeManualLocation(manualLocation);
            }
            
            // Check again after attempt
            if (selectedLatitude == 0 && selectedLongitude == 0) {
                Toast.makeText(this, "Please select a valid location using search or 'Get Current Location'", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Validation failed: Location coordinates missing");
                return false;
            }
        }
        
        Log.d(TAG, "Validation successful");
        return true;
    }

    private void geocodeManualLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLatitude = address.getLatitude();
                selectedLongitude = address.getLongitude();
                Log.d(TAG, "Geocoded manual input: " + locationName + " to " + selectedLatitude + ", " + selectedLongitude);
            } else {
                Log.w(TAG, "Geocoder found no results for: " + locationName);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
        }
    }

    private boolean isFieldValid(TextInputEditText field, String fieldName) {
        if (field.getText() == null || field.getText().toString().trim().isEmpty()) {
            field.setError("Please enter " + fieldName);
            field.requestFocus();
            return false;
        }
        return true;
    }

    private void saveItem() {
        try {
            Log.d(TAG, "Saving item to database...");
            String imagePath = ImageUtils.saveImageToInternalStorage(this, selectedImageUri);

            if (imagePath == null || imagePath.isEmpty()) {
                Toast.makeText(this, "Failed to save image. Please try another image.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Image saving failed");
                return;
            }

            String type = radioLost.isChecked() ? "lost" : "found";
            String category = spinnerCategory.getSelectedItem().toString();

            Item item = new Item(
                    editName.getText().toString().trim(),
                    editDescription.getText().toString().trim(),
                    category,
                    type,
                    editLocation.getText().toString().trim(),
                    editPhone.getText().toString().trim(),
                    editDate.getText().toString().trim(),
                    imagePath,
                    selectedLatitude,
                    selectedLongitude
            );

            long id = databaseHelper.insertItem(item);
            if (id != -1) {
                Log.d(TAG, "Item saved successfully with ID: " + id);
                Toast.makeText(this, "Success! Item saved with ID: " + id, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e(TAG, "Database insertion failed");
                Toast.makeText(this, "Error: Could not save to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during save: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}