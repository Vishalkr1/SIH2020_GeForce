package com.example.hack2020;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class posthelp extends HomeActivity {

    Button emergency;
    Button ambulance;
    Button covid19help;
    Button doctor;
    Button emergency3;
    public String phonenums;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posthelp);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        db.collection("Users").document(mAuth.getCurrentUser().getEmail()).collection("people").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        phonenums=document.getString("phoneNum");
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        emergency = findViewById(R.id.emergency);
        ambulance = findViewById(R.id.ambulance);
        covid19help = findViewById(R.id.covid19help);
        doctor = findViewById(R.id.doctor);
        emergency3 = findViewById(R.id.emergency3);
    }

    public void onDialPoison(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:1075"));
        startActivity(intent);

    }

    public void onDialEmergency(View v) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:100"));
        startActivity(intent);

    }

    public void onDialEmergencyManagement(View v) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:108"));
        startActivity(intent);

    }
    public void onDialemergency3(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phonenums, null));
        startActivity(intent);
    }
    public void onDialEmergency2(View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:1800-180-1253"));
        startActivity(intent);
  }

}
