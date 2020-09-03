package com.example.hack2020;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "Sample";
    TextView textView;
    Button btn;
    TextView rate;
    TextView temper;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Button med;
    String temp;
    String hat;


    public Bundle bundle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        textView = findViewById(R.id.textView2);
        btn = findViewById(R.id.geofenceBtn);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        temper = findViewById(R.id.tempValTV);
        rate = findViewById(R.id.heartValTV);
        bundle = getIntent().getExtras();
        if(bundle != null) {
            db.collection("Users").document(bundle.getString("Output")).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null) {
                                textView.setText(documentSnapshot.getString("name"));
                            }
                        }
                    });
            db.collection("Users").document(bundle.getString("Output")).collection("My data")
                    .document("Health params").addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.contains("Body temperature") && documentSnapshot.contains("Heart rate")){
                        final String temp = documentSnapshot.get("Body temperature").toString();
                        final String hat = documentSnapshot.get("Heart rate").toString();
                        temper.setText(temp);
                        rate.setText(hat);
                        String s=hat;
                        String b = temp;
                        int t=Integer.parseInt(s.replaceAll("[\\D]", ""));
                        int h = Integer.parseInt(b.replaceAll("[\\D]", ""));
                        if(!(!((h>=36.1) && (h<=37.2))|| (!((t>=60) && (t<=100))))){
                            //Intent intent = new Intent(DashboardActivity.this,ApproachCareReceiver.class);
                        }
                    }
                }
            });
        }
        if(bundle != null){
            textView.setText(bundle.getString("Output"));
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, RetrieveMapActivity.class);
                assert bundle != null;
                intent.putExtra("CRemail", bundle.getString("Output"));
                startActivity(intent);
            }
        });

    }
}