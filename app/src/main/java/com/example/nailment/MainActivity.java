package com.example.nailment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private static final int SEARCH_RADIUS = 2000; // 5km radius
    private List<ImageView> salonImageViews;
    private List<TextView> salonNameViews;
    private List<String> salonPhotoUrls;
    private List<String> salonNames;
    private List<ImageView> designImageViews;
    private static final String SEARCH_QUERY = "manicurist nail designs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Places API
        if (!Places.isInitialized()) {
            String apiKey = getString(R.string.google_maps_key);
            if (apiKey.isEmpty()) {
                Toast.makeText(this, "Google Maps API key is missing", Toast.LENGTH_LONG).show();
                return;
            }
            Places.initialize(getApplicationContext(), apiKey);
        }
        PlacesClient placesClient = Places.createClient(this);

        // Initialize fused location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize ImageViews and TextViews
        salonImageViews = new ArrayList<>();
        salonImageViews.add(findViewById(R.id.logo1));
        salonImageViews.add(findViewById(R.id.logo2));
        salonImageViews.add(findViewById(R.id.logo3));
        salonImageViews.add(findViewById(R.id.logo4));

        salonNameViews = new ArrayList<>();
        salonNameViews.add(findViewById(R.id.salonName1));
        salonNameViews.add(findViewById(R.id.salonName2));
        salonNameViews.add(findViewById(R.id.salonName3));
        salonNameViews.add(findViewById(R.id.salonName4));

        designImageViews = new ArrayList<>();
        designImageViews.add(findViewById(R.id.design1));
        designImageViews.add(findViewById(R.id.design2));
        designImageViews.add(findViewById(R.id.design3));
        designImageViews.add(findViewById(R.id.design4));

        fetchNailDesignImages();

        salonPhotoUrls = new ArrayList<>();
        salonNames = new ArrayList<>();

        // Check location permission and fetch salon photos
        checkLocationPermission();

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Camera button to open CameraActivity
        findViewById(R.id.cameraButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        // Add button click event for navigation to MapActivity
        Button navigateToMapButton = findViewById(R.id.navigateToMapButton);
        navigateToMapButton.setOnClickListener(v -> {
            String apiKey = getString(R.string.google_maps_key);
            if (apiKey.isEmpty()) {
                Toast.makeText(MainActivity.this, "Google Maps API key is missing", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Book procedure button to navigate to ListActivity
        findViewById(R.id.bookProcedureButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });

        // Chat button to navigate to ChatActivity
        Button chatButton = findViewById(R.id.chatButton);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Profile button to navigate to UserProfileActivity
        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "You must be logged in to view your profile", Toast.LENGTH_SHORT).show();
                // Optionally navigate to login screen
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
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
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + userLocation.latitude + "," + userLocation.longitude +
                "&radius=" + SEARCH_RADIUS +
                "&type=beauty_salon" +
                "&key=" + apiKey;

        new Thread(() -> {
            try {
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

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                salonPhotoUrls.clear();
                salonNames.clear();

                for (int i = 0; i < results.length() && salonNames.size() < 4; i++) {
                    JSONObject place = results.getJSONObject(i);
                    String name = place.getString("name");

                    // Always add a name
                    salonNames.add(name);

                    if (place.has("photos")) {
                        JSONArray photos = place.getJSONArray("photos");
                        if (photos.length() > 0) {
                            String photoReference = photos.getJSONObject(0).getString("photo_reference");
                            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                    "?maxwidth=400" +
                                    "&photo_reference=" + photoReference +
                                    "&key=" + apiKey;
                            salonPhotoUrls.add(photoUrl);
                        } else {
                            salonPhotoUrls.add(null); // Ensures the index alignment
                        }
                    } else {
                        salonPhotoUrls.add(null); // Ensures the index alignment
                    }
                }


                runOnUiThread(this::updateSalonImages);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching places", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to load salon photos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateSalonImages() {
        for (int i = 0; i < salonImageViews.size(); i++) {
            ImageView imageView = salonImageViews.get(i);
            TextView nameView = salonNameViews.get(i);

            if (i < salonNames.size()) {
                String salonName = salonNames.get(i);
                nameView.setText(salonName);
                nameView.setVisibility(View.VISIBLE);
                
                // Add click listener to open Google search for the salon
                final String finalSalonName = salonName;
                imageView.setOnClickListener(v -> {
                    String searchQuery = "https://www.google.com/search?q=" + Uri.encode(finalSalonName + " nail salon");
                    openWebPage(searchQuery);
                });
            } else {
                nameView.setVisibility(View.GONE);
                // Remove click listener if no salon name
                imageView.setOnClickListener(null);
            }

            if (i < salonPhotoUrls.size() && salonPhotoUrls.get(i) != null) {
                String photoUrl = salonPhotoUrls.get(i);
                Glide.with(this).load(photoUrl).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_image); // Default image
            }
        }
    }

    private void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
    private void fetchNailDesignImages() {
        String apiKey = getString(R.string.google_search_key);
        String cx = getString(R.string.google_search_cx);
        String url = "https://www.googleapis.com/customsearch/v1?q=" + SEARCH_QUERY +
                "&cx=" + cx + "&searchType=image&num=4&key=" + apiKey;

        new Thread(() -> {
            try {
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

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray items = jsonResponse.getJSONArray("items");

                List<String> imageUrls = new ArrayList<>();
                for (int i = 0; i < items.length() && i < 4; i++) {
                    imageUrls.add(items.getJSONObject(i).getString("link"));
                }

                runOnUiThread(() -> updateDesignImages(imageUrls));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching nail design images", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to load nail designs", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    private void updateDesignImages(List<String> imageUrls) {
        for (int i = 0; i < designImageViews.size(); i++) {
            if (i < imageUrls.size()) {
                Glide.with(this).load(imageUrls.get(i)).into(designImageViews.get(i));
            } else {
                designImageViews.get(i).setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}