package com.example.hack2020;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntermediateActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("User data"); //Does Users -> Groups
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart(){
        super.onStart();
        if (mAuth.getCurrentUser() == null){
            Intent intent = new Intent(IntermediateActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (mAuth.getCurrentUser() != null){
            db.collection("Users").document(mAuth.getCurrentUser().getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null){
                        if (documentSnapshot.getString("userType")!= null && documentSnapshot.getString("userType").equals("Care Receiver")){
                            Intent intent = new Intent(IntermediateActivity.this,HomeActivity.class);
                            startActivity(intent);
                        }
                        else if(documentSnapshot.getString("userType")!= null && documentSnapshot.getString("userType").equals("Care Taker"))
                        {
                            Intent intent = new Intent(IntermediateActivity.this,CareGiverAddCareReciever.class);
                            startActivity(intent);
                        }
                    }

                }
            });
        }
    }
}