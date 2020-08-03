package com.example.hack2020;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
    public Bundle bundle;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        textView = findViewById(R.id.textView2);
        btn = findViewById(R.id.retreiveMap);
        bundle = getIntent().getExtras();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if(bundle != null){
            db.collection("Users").document(bundle.getString("Output")).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null){
                                textView.setText(documentSnapshot.getString("name"));
                            }
                        }
                    });

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