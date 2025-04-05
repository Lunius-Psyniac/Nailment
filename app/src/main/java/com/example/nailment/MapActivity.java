package com.example.nailment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.PlaceTypes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private SearchView searchView;
    private static final int SEARCH_RADIUS = 5000; // 5km radius

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        try {
            // Initialize Places API
            if (!Places.isInitialized()) {
                String apiKey = getString(R.string.google_maps_key);
                if (apiKey.isEmpty()) {
                    Toast.makeText(this, "Google Maps API key is missing", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Places.initialize(getApplicationContext(), apiKey);
            }
            placesClient = Places.createClient(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Places API", e);
            Toast.makeText(this, "Error initializing Places API: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
            Toast.makeText(this, "Error loading map", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            if (mMap == null) {
                Log.e(TAG, "GoogleMap is null after assignment");
                Toast.makeText(this, "Error initializing map", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            
            mMap.setOnMarkerClickListener(this::onMarkerClick);
            checkLocationPermission();
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady", e);
            Toast.makeText(this, "Error initializing map: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request both permissions
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getUserLocation();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(userLocation)
                                    .title("Your Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            // Automatically search for salons when location is obtained
                            searchNearbySalons(userLocation);
                        } else {
                            Toast.makeText(this, "Failed to get location. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location", e);
                        Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void searchNearbySalons(LatLng userLocation) {
        String apiKey = getString(R.string.google_maps_key);
        int radius = SEARCH_RADIUS; // 5km radius
        String type = "beauty_salon"; // Google Places API type
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + userLocation.latitude + "," + userLocation.longitude +
                "&radius=" + radius +
                "&type=" + type +
                "&key=" + apiKey;

        new Thread(() -> {
            try {
                // Make HTTP request
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                runOnUiThread(() -> {
                    mMap.clear(); // Clear old markers
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                    for (int i = 0; i < results.length(); i++) {
                        try {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                            String name = place.getString("name");
                            String address = place.optString("vicinity", "No address available");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");

                            LatLng placeLocation = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(placeLocation)
                                    .title(name)
                                    .snippet(address));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing place data", e);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching places", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to load nearby salons", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private boolean isSalon(Place place) {
        if (place.getTypes() == null) return false;

        // Keywords that indicate a salon
        String[] salonKeywords = {"salon", "beauty", "hair", "spa", "barber"};
        String placeName = place.getName().toLowerCase();

        // Check if the place name contains any salon keywords
        for (String keyword : salonKeywords) {
            if (placeName.contains(keyword)) {
                return true;
            }
        }

        // Check place types
        for (Place.Type type : place.getTypes()) {
            if (type == Place.Type.BEAUTY_SALON || type == Place.Type.HAIR_CARE) {
                return true;
            }
        }

        return false;
    }

    private boolean onMarkerClick(Marker marker) {
        showMarkerDialog(marker);
        return true;
    }

    private void showMarkerDialog(Marker marker) {
        new AlertDialog.Builder(this)
                .setTitle(marker.getTitle())
                .setMessage(marker.getSnippet())
                .setPositiveButton("OK", null)
                .show();
    }
}