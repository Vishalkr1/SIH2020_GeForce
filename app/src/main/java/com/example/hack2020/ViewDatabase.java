package com.example.hack2020;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by User on 2/8/2017.
 */

public class ViewDatabase extends AppCompatActivity {
    private static final String TAG = "ViewDatabase";

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private  String userID;

    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_database_layout);

        mListView = (ListView) findViewById(R.id.listview);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user != null ? user.getUid() : "default_name";

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
                // ...
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            UserInformation uInfo = new UserInformation();
            LocationHelper location = new LocationHelper();
//            HealthdataHelper health = new HealthdataHelper();
            uInfo.setName(ds.child(userID).child("User data").getValue(UserInformation.class).getName()); //set the name
            uInfo.setEmail(ds.child(userID).child("User data").getValue(UserInformation.class).getEmail()); //set the email
            uInfo.setUsertype(ds.child(userID).child("User data").getValue(UserInformation.class).getUsertype());
            location.setLongitude(ds.child(userID).child("Current location").getValue(LocationHelper.class).getLongitude());
            location.setLatitude(ds.child(userID).child("Current location").getValue(LocationHelper.class).getLatitude());
//            health.setTemperature(ds.child(userID).child("Health data").getValue(HealthdataHelper.class).getTemperature());
//            health.setHeartRate(ds.child(userID).child("Health data").getValue(HealthdataHelper.class).getHeartRate());
 //           uInfo.setPhone_num(ds.child(userID).getValue(UserInformation.class).getPhone_num()); //set the phone_num

            //display all the information
            Log.d(TAG, "showData: name: " + uInfo.getName());
            Log.d(TAG, "showData: email: " + uInfo.getEmail());
//            Log.d(TAG, "showData: Body temp: " + health.getTemperature());
//            Log.d(TAG, "showData: Heart rate: " + health.getHeartRate());
            Log.d(TAG, "showData: Current location: " + location.getLongitude() + "," + location.getLatitude());
 //           Log.d(TAG, "showData: phone_num: " + uInfo.getPhone_num());

            ArrayList array  = new ArrayList<>();
            array.add("User: " + uInfo.getName());
            array.add("Email: " + uInfo.getEmail());
            array.add("User type: " + uInfo.getUsertype());
//            array.add("Body Temperature: " + health.getTemperature());
//            array.add("Heart Rate: " + health.getHeartRate());
            array.add("Last known location: " + location.getLongitude() +", "+ location.getLatitude());
   //         array.add(uInfo.getPhone_num());

            ArrayAdapter adapter;
            adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,array);
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
