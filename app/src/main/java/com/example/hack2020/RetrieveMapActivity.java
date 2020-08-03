package com.example.hack2020;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RetrieveMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
        RadiusDialog.ExampleDialogListener{

    private static final String TAG = "Sample";
    private static final String CHANNEL_ID = "NOTIFICATION";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LatLng location;
    private Bundle bundle;
    private Double latitude, longitude;
    private Double lat, lon;
    private AutoCompleteTextView mSearch;
    private ImageView imageButton;
    private int GEOFENCE_RADIUS = 200;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private String GEOFENCE_ID = "GEOFENCE_ID";
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private double start_latitude, start_longitude;
    private double end_latitude, end_longitude;
    private String current_user;
    private double lmlatitude, lmlongitude;
    private int GeofenceRadius;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_map);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        current_user = mAuth.getCurrentUser().getEmail();
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
        this.lmlatitude = 28.6139;
        this.lmlongitude = 77.2090;
        getLandmark();
    }

    private  LatLng getCurrentLocation(){

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
                            end_latitude = documentSnapshot.getDouble("latitude");
                            end_longitude = documentSnapshot.getDouble("longitude");
                            Log.d(TAG, "onEvent: " + end_latitude);
                            Log.d(TAG, "onEvent: " + end_longitude);

                            LatLng location = new LatLng(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"));
                            MarkerOptions mMarker = new MarkerOptions().position(location)
                                    .title(getCOmpleteAddress(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude")));
                            if(getDistance() <= GEOFENCE_RADIUS) {
                                mMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                            else{
                                mMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }

                            mMap.addMarker(mMarker);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16F));
                        } else {
                            Log.e(TAG, "onEvent: Document snapshot was null");
                        }

                    }
                });
        return location;

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
//        mMap.clear();
        getCurrentLocation();
        start_latitude = latLng.latitude;
        start_longitude = latLng.longitude;
        getDistance();
        Log.d(TAG, "Distance is " + getDistance());
        addMarker(latLng);
        Log.d(TAG, "handleMapLongClick: " + GEOFENCE_RADIUS);
        addCircle(latLng, getRadius());
        addGeofence(latLng);
        saveLandmark(latLng);
        openDialog();

    }


    private void openDialog() {
        RadiusDialog exampledialog = new RadiusDialog();
        exampledialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void saveLandmark(LatLng latLng){

        HashMap<String, Double> landmark = new HashMap<>();
        landmark.put("latitude", latLng.latitude);
        landmark.put("longitude", latLng.longitude);
        CollectionReference collectionReference = db.collection("Users");
        collectionReference.document(mAuth.getCurrentUser().getEmail())
                .collection("people").document(bundle.getString("CRemail"))
                .collection("landmark").document("landmark")
                .set(landmark).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RetrieveMapActivity.this,"Landmark updated", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onSuccess: Landmark updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RetrieveMapActivity.this,"Error updating Landmark",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: Landmark update failed " + e.toString());
            }
        });

    }

    private void getLandmark(){


        db.collection("Users").document(mAuth.getCurrentUser().getEmail()).collection("people")
                .document(bundle.getString("CRemail")).collection("landmark").document("landmark").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                    return;
                }
                if (documentSnapshot != null) {
                    if (documentSnapshot.contains("latitude") && documentSnapshot.contains("longitude")) {
                        lmlatitude = documentSnapshot.getDouble("latitude");
                        lmlongitude = documentSnapshot.getDouble("longitude");
                        Log.d(TAG, "onEvent: " + longitude);

                        LatLng location = new LatLng(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"));
                        onMapLongClick(location);
                    }
                }else {
                    Log.e(TAG, "onEvent: Document snapshot was null");
                }


            }

        });
    }

    private float getDistance(){
        float result[] = new float[10];
        Location.distanceBetween(start_latitude,start_longitude
                ,end_latitude, end_longitude,result);
        return result[0];
    }

    private void addGeofence(LatLng latLng){
        start_latitude = latLng.latitude;
        start_longitude = latLng.longitude;
        float distance = getDistance();
        if(distance >= GEOFENCE_RADIUS){
            Toast.makeText(this,"Care Receiver is outside Geofence", Toast.LENGTH_LONG).show();
            pushNotification("Alert","Alert!! Care receiver is outside the geofence boundary, reach the care receiver asap.");
        }
        else{
            Toast.makeText(this,"Care Receiver is inside Geofence", Toast.LENGTH_LONG).show();
        }
    }


    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(getCOmpleteAddress(latLng.latitude, latLng.longitude));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(markerOptions);
        Toast.makeText(this,"Care receiver is " + getDistance() + "m away from landmark",Toast.LENGTH_SHORT).show();

    }
    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(50,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);

    }

    private void pushNotification(String title,String Content){
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(getApplicationContext(), ApproachCareReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle data = new Bundle();
        data.putString("email", bundle.getString("CRemail"));
        intent.putExtras(data);
        startActivity(intent);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(Content)// message for notification
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(pi, true)
                .setAutoCancel(autocancel()); // clear notification after click
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
    private boolean autocancel(){
        if(getDistance() >= GEOFENCE_RADIUS){
            return false;
        }
        else {
            return true;
        }
    }

    private int getRadius(){


        db.collection("Users").document(mAuth.getCurrentUser().getEmail()).collection("people")
                .document(bundle.getString("CRemail")).collection("landmark").document("landmark radius").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                    return;
                }
                if (documentSnapshot != null) {
                    if (documentSnapshot.contains("Radius")) {
//                        GEOFENCE_RADIUS = ((Long) documentSnapshot.get("Radius")).intValue();
                        Log.d(TAG, "onEvent: " + GEOFENCE_RADIUS);
                    }
                }else {
                    Log.e(TAG, "onEvent: Document snapshot was null");
                }


            }

        });

        return GEOFENCE_RADIUS;


    }

    @Override
    public void applyText(String radius) {
        GeofenceRadius = Integer.parseInt(radius);
        GEOFENCE_RADIUS = GeofenceRadius;

    }
}

