package com.example.hack2020;


import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;
import java.util.Locale;

public class RetrieveMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "Sample";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LatLng location;
    private Bundle bundle;
    private Double latitude, longitude;
    private Double lat, lon;
    private AutoCompleteTextView mSearch;
    private ImageView imageButton;
    private float GEOFENCE_RADIUS = 200;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private String GEOFENCE_ID = "GEOFENCE_ID";
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_map);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        mSearch = (AutoCompleteTextView) findViewById(R.id.input_search);
        imageButton = (ImageView) findViewById(R.id.imageButton);
        bundle = getIntent().getExtras();
        Log.d(TAG, "onCreate: " + bundle.getString("CRemail"));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        this.latitude = 28.6139391;
        this.longitude = 77.2068325;
        getCurrentLocation();


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });
        mMap.setOnMapLongClickListener(this);
    }

    private  void getCurrentLocation(){

        db.collection("Users").document(bundle.getString("CRemail"))
                .collection("My data")
                .document("Current location")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: ", e);
                            return;
                        }
                        if (documentSnapshot != null) {
                            Log.d(TAG, "onEvent: ===================================");
                            latitude = documentSnapshot.getDouble("latitude");
                            longitude = documentSnapshot.getDouble("longitude");
                            Log.d(TAG, "onEvent: " + latitude);
                            Log.d(TAG, "onEvent: " + longitude);

                            LatLng location = new LatLng(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"));

                            mMap.addMarker(new MarkerOptions().position(location).title(getCOmpleteAddress(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"))));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14F));
                        } else {
                            Log.e(TAG, "onEvent: Document snapshot was null");
                        }

                    }
                });

    }


    private String getCOmpleteAddress(double Latitude, double Longtitude) {

        String address = "";

        Geocoder geocoder = new Geocoder(RetrieveMapActivity.this, Locale.getDefault());

        try {

            List<Address> addresses = geocoder.getFromLocation(Latitude, Longtitude, 1);

            if (address != null) {

                Address returnAddress = addresses.get(0);
                StringBuilder stringBuilderReturnAddress = new StringBuilder("");

                for (int i = 0; i <= returnAddress.getMaxAddressLineIndex(); i++) {
                    stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n");
                }

                address = stringBuilderReturnAddress.toString();

            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


        return address;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {


        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            handleMapLongClick(latLng);
        }

    }

    private void handleMapLongClick(LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
    }

    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER
                | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence added");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                            String errorMesasge = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: "+ errorMesasge);
                    }
                });

    }

    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(getCOmpleteAddress(latLng.latitude,latLng.longitude));
        mMap.addMarker(markerOptions);
        getCurrentLocation();
    }
    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(64,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);

    }
}

