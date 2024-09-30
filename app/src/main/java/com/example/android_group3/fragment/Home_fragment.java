package com.example.android_group3.fragment;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.android_group3.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.google.android.gms.location.LocationCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Home_fragment extends Fragment{

    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private FloatingActionButton floatingActionButtonVehicle;
    private FloatingActionButton floatingActionButtonDirection;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvStatus;
    private Double userLatitude = 0.0;
    private Double userLongitude = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapboxDirections client;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Toast.makeText(getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Instantiate the Route Line API and View

        floatingActionButton = view.findViewById(R.id.focusLocation);
        floatingActionButtonVehicle = view.findViewById(R.id.focusVehicle);
        floatingActionButtonDirection = view.findViewById(R.id.focusDirection);
        tvLatitude = view.findViewById(R.id.tv_latitude);
        tvLongitude = view.findViewById(R.id.tv_longitude);
        tvStatus = view.findViewById(R.id.tv_status);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return view;
        }

        String email = user.getEmail();
        if (email == null || !email.contains("@")) {
            Toast.makeText(getContext(), "Invalid email address.", Toast.LENGTH_SHORT).show();
            return view;
        }
        String username = email.split("@")[0];

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userLocation = database.getReference(username + "/feature");

        userLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLatitude = dataSnapshot.child("Latitude").getValue(Double.class);
                tvLatitude.setText(String.valueOf(userLatitude));
                userLongitude = dataSnapshot.child("Longitude").getValue(Double.class);
                tvLongitude.setText(String.valueOf(userLongitude));
                String userStatus = dataSnapshot.child("Status").getValue(String.class);
                tvStatus.setText(userStatus);

                updateMapLocation(userLatitude, userLongitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to read data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mbMap) {
                mapboxMap = mbMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        style.addImage("red-pin-icon-id", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_location)));
                        style.addImage("home-icon-id", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_location2)));
                        GeoJsonSource firebaseSource = new GeoJsonSource("firebase-source-id", Feature.fromGeometry(Point.fromLngLat(userLongitude, userLatitude)));
                        style.addSource(firebaseSource);
                        style.addLayer(new SymbolLayer("firebase-layer-id", "firebase-source-id").withProperties(
                                iconImage("red-pin-icon-id"),
                                iconIgnorePlacement(true),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{0f, -9f})
                        ));

                        moveCamera(userLatitude, userLongitude);
                        Point destination = Point.fromLngLat(userLongitude, userLatitude);

                    }
                });
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserLocation();
            }
        });

        floatingActionButtonVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMapLocation(userLatitude, userLongitude);
            }
        });

        floatingActionButtonDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDirections();
            }
        });

        return view;
    }

    private void getDirections() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();

                // Update the route from Firebase to phone's current location
                updateMapWithTwoLocations(userLatitude, userLongitude, currentLatitude, currentLongitude);
                getRoute(mapboxMap, Point.fromLngLat(userLongitude, userLatitude), Point.fromLngLat(currentLongitude, currentLatitude));
            } else {
                Toast.makeText(getContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.body() == null || response.body().routes().isEmpty()) {
                    Toast.makeText(getContext(), "No route found", Toast.LENGTH_SHORT).show();
                    return;
                }

                DirectionsRoute currentRoute = response.body().routes().get(0);
                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        GeoJsonSource routeSource = style.getSourceAs("route-source-id");

                        if (routeSource == null) {
                            routeSource = new GeoJsonSource("route-source-id", LineString.fromPolyline(currentRoute.geometry(), Constants.PRECISION_6));
                            style.addSource(routeSource);

                            // Draw the route as a line layer
                            LineLayer routeLayer = new LineLayer("route-layer-id", "route-source-id")
                                    .withProperties(
                                            lineColor(Color.parseColor("#3b9ddd")),
                                            lineWidth(5f)
                                    );
                            style.addLayer(routeLayer);
                        } else {
                            routeSource.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), Constants.PRECISION_6));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Toast.makeText(getContext(), "Error retrieving route: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



//    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response){
//        if (response.body() == null){
//            Toast.makeText(getContext(), "No route found", Toast.LENGTH_SHORT).show();
//            return;
//        }else if (response.body().routes().size() < 1){
//            Toast.makeText(getContext(), "No route found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        DirectionsRoute currentRoute = response.body().routes().get(0);
//
//        if (mapboxMap != null){
//            mapboxMap.getStyle(new Style.OnStyleLoaded){
//                @Override
//                public void onStyleLoaded(@NonNull Style style) {
//                    GeoJsonSource source = style.getSourceAs("route-source-id");
//                    if (source == null){
//                        source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), Constants.PRECISION_6));
//                    }
//                }
//            });
//        }
//    }


    // Method to update the map location
    private void updateMapLocation(Double latitude, Double longitude) {
        if (mapboxMap != null && mapboxMap.getStyle() != null) {
            // Update the GeoJsonSource with the new location
            GeoJsonSource firebaseSource = mapboxMap.getStyle().getSourceAs("firebase-source-id");
            if (firebaseSource != null) {
                firebaseSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)));
            } else {
                // If the source doesn't exist, create it
                firebaseSource = new GeoJsonSource("firebase-source-id", Feature.fromGeometry(Point.fromLngLat(longitude, latitude)));
                mapboxMap.getStyle().addSource(firebaseSource);
                mapboxMap.getStyle().addLayer(new SymbolLayer("firebase-layer-id", "firebase-source-id").withProperties(
                        iconImage("red-pin-icon-id"),
                        iconIgnorePlacement(true),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{0f, -9f})
                ));
            }

            // Move the camera to the new location
            moveCamera(latitude, longitude);
        }
    }


    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();

                updateMapWithTwoLocations(userLatitude, userLongitude, currentLatitude, currentLongitude);
                moveCamera(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(getContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapWithTwoLocations(Double firebaseLatitude, Double firebaseLongitude, Double currentLatitude, Double currentLongitude) {
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                GeoJsonSource firebaseSource = style.getSourceAs("firebase-source-id");
                if (firebaseSource == null) {
                    firebaseSource = new GeoJsonSource("firebase-source-id", Feature.fromGeometry(Point.fromLngLat(firebaseLongitude, firebaseLatitude)));
                    style.addSource(firebaseSource);
                    style.addLayer(new SymbolLayer("firebase-layer-id", "firebase-source-id").withProperties(
                            iconImage("red-pin-icon-id"),
                            iconIgnorePlacement(true),
                            iconAllowOverlap(true),
                            iconOffset(new Float[]{0f, -9f})
                    ));
                } else {
                    firebaseSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(firebaseLongitude, firebaseLatitude)));
                }

                GeoJsonSource currentSource = style.getSourceAs("current-source-id");
                if (currentSource == null) {
                    currentSource = new GeoJsonSource("current-source-id", Feature.fromGeometry(Point.fromLngLat(currentLongitude, currentLatitude)));
                    style.addSource(currentSource);
                    style.addLayer(new SymbolLayer("current-layer-id", "current-source-id").withProperties(
                            iconImage("home-icon-id"),
                            iconIgnorePlacement(true),
                            iconAllowOverlap(true),
                            iconOffset(new Float[]{0f, -9f})
                    ));
                } else {
                    currentSource.setGeoJson(Feature.fromGeometry(Point.fromLngLat(currentLongitude, currentLatitude)));
                }
            }
        }
    }

    private void moveCamera(Double latitude, Double longitude) {
        mapboxMap.setCameraPosition(new com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
                .target(new com.mapbox.mapboxsdk.geometry.LatLng(latitude, longitude))
                .zoom(17)
                .build());
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

}