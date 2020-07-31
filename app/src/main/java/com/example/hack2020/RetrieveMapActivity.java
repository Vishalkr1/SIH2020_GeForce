package com.example.hack2020;


import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RetrieveMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Sample";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LatLng location;
    private Bundle bundle;
    private Double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_map);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        bundle = getIntent().getExtras();
        Log.d(TAG, "onCreate: " + bundle.getString("CRemail"));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        this.latitude=28.6139391;
        this.longitude=77.2068325;

        //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Current Location");//get from child node od user
        db.collection("Users").document(bundle.getString("CRemail"))
                .collection("My data")
                .document("Current location")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e!= null){
                            Log.e(TAG, "onEvent: ", e);
                            return;
                        }
                        if(documentSnapshot != null){
                            Log.d(TAG, "onEvent: ===================================");
                                latitude = documentSnapshot.getDouble("latitude");
                                longitude = documentSnapshot.getDouble("longitude");
                            Log.d(TAG, "onEvent: "+ latitude);
                            Log.d(TAG, "onEvent: "+ longitude);

                            LatLng location = new LatLng(latitude, longitude);

                            mMap.addMarker(new MarkerOptions().position(location).title(getCOmpleteAddress(latitude, longitude)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,14F));
                        } else{
                            Log.e(TAG, "onEvent: Document snapshot was null");
                        }

                    }
                });

    }


    private String getCOmpleteAddress(double Latitude,double Longtitude){

        String address = "";

        Geocoder geocoder = new Geocoder(RetrieveMapActivity.this,Locale.getDefault());

        try{

            List<Address> addresses = geocoder.getFromLocation(Latitude,Longtitude,1);

            if(address!=null){

                Address returnAddress = addresses.get(0);
                StringBuilder stringBuilderReturnAddress =  new StringBuilder("");

                for(int i=0; i<=returnAddress.getMaxAddressLineIndex();i++){
                    stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n");
                }

                address = stringBuilderReturnAddress.toString();

            }
            else{
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


        return address;
    }

}

