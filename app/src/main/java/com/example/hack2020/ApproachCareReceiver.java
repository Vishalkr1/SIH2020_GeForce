package com.example.hack2020;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApproachCareReceiver extends AppCompatActivity {

    private static final String TAG = "ApproachCareReceiver";
    private Button btnNavigate, call;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LatLng origin, desination;
    public Bundle mbundle;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_approach_care_receiver);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mbundle = getIntent().getExtras();
        Log.d(TAG, "onCreate: " + mbundle.getString("email"));
        call = findViewById(R.id.calling);
        btnNavigate = findViewById(R.id.navigate);
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ApproachCareReceiver.this, DirectionMap.class);
                intent.putExtra("receiverEmail", mbundle.getString("email"));
                startActivity(intent);

//                DisplayTrack(getOrigin(), getDesination());
//                String url = getRequestURL(getOrigin(), getDesination());
//                TaskRequestDirection taskRequestDirection = new TaskRequestDirection();
//                taskRequestDirection.execute(url);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:6203831026"));
                startActivity(intent);
            }
        });
    }

    private void DisplayTrack(LatLng origin, LatLng desination) {
        try {
            Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + getOrigin() +"&daddr=" + getDestination());
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e){
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private String getRequestURL(LatLng origin, LatLng dest){
        String str_org = "origin=" + origin.latitude + ","+ origin.longitude;
        String str_dest = "destination=" + dest.latitude + ","+ dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_org +"&"+ str_dest+"&"+sensor+mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    private LatLng getOrigin() {
        if (mbundle != null) {
            db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                    .collection("My data").document("Current location").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.contains("latitude") && documentSnapshot.contains("longitude")) {
                            origin = new LatLng(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"));
                            Log.d(TAG, "onEvent: origin " + origin);
                        }
                    }
                }
            });


        }
        return origin;
    }

    private LatLng getDestination() {


            if (mbundle != null) {
                db.collection("Users").document(mbundle.getString("email")).collection("My data")
                        .document("Current location").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.contains("latitude") && documentSnapshot.contains("longitude")) {
                                desination = new LatLng(documentSnapshot.getDouble("latitude"), documentSnapshot.getDouble("longitude"));
                                Log.d(TAG, "onEvent: destination" + desination);
                            }
                        }
                    }
                });

            }
        return desination;
        }



    private String getDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
         try {
             URL url = new URL(reqUrl);
             httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.connect();

             inputStream = httpURLConnection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

             StringBuffer stringBuffer = new StringBuffer();
             String line = "";
             while ((line = bufferedReader.readLine())!= null){
                 stringBuffer.append(line);
             }

             responseString = stringBuffer.toString();
             bufferedReader.close();
             inputStreamReader.close();

         } catch (MalformedURLException e) {
             e.printStackTrace();
         } finally {
             if(inputStream != null){
                 inputStream.close();
             }
             httpURLConnection.disconnect();
         }
         return responseString;
    }

    public class TaskRequestDirection extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = getDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List <HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsJSONParser directionsJSONParser = new DirectionsJSONParser();
                routes =  directionsJSONParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList poinits = null;
            PolylineOptions polylineOptions = null;

            for(List<HashMap<String, String>> path: lists){
                poinits = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point: path){
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    poinits.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(poinits);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

             if(polylineOptions != null){
                 mMap.addPolyline(polylineOptions);
             }else{
                 Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
             }
        }
    }



}
