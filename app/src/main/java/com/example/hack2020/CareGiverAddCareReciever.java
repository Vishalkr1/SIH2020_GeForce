package com.example.hack2020;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import users.User;

public class CareGiverAddCareReciever extends AppCompatActivity implements Dialog.ExampleDialogListener {
    private static final String TAG = "MainActivity";
    private FloatingActionButton button;
    private Button btnLogout;
    private String CareReceiverEmail;

    private ArrayList<User> careReceivers;
    private CareGiverAdapter adapter;
    ListView careReceiverListView;
    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_giver_add_care_reciever);
        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();

        button = findViewById(R.id.addUser);
        careReceiverListView = findViewById(R.id.care_receiver_listview);
        getCareTakers();

        db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                .collection("people").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    Log.d(TAG, list.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        careReceiverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CareGiverAddCareReciever.this,DashboardActivity.class);
                intent.putExtra("Output", list.get(i));
                startActivity(intent);
            }
        });


        btnLogout = findViewById(R.id.button2);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent inToMain = new Intent(CareGiverAddCareReciever.this, MainActivity.class);
                startActivity(inToMain);
                finish();
            }
        });
    }



    public void openDialog() {
        Dialog exampleDialog = new Dialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String userEmail) {
        CareReceiverEmail = userEmail;
        if (!userEmail.equals("")) {
            DocumentReference documentReference = db.collection("Users").document(userEmail);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.getString("userType").equals("Care Receiver")) {
                                HashMap<String, String> data = new HashMap<>();
                                data.put("name", documentSnapshot.getString("name"));
                                data.put("phoneNum", documentSnapshot.getString("phoneNum"));
                                db.collection("Users").document(mAuth.getCurrentUser().getEmail())
                                        .collection("people").document(CareReceiverEmail).set(data);
                                getCareTakers();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getEmail());
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnap = task.getResult();
                                        if (documentSnap.exists()) {
                                            if (documentSnap.getString("userType").equals("Care Taker")) {
                                                HashMap<String, String> data = new HashMap<>();
                                                data.put("name", documentSnap.getString("name"));
                                                data.put("phoneNum", documentSnap.getString("phoneNum"));
                                                db.collection("Users").document(CareReceiverEmail)
                                                        .collection("people").document(mAuth.getCurrentUser().getEmail()).set(data);
                                                getCareTakers();
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                }
                            });
                }
            });
        }
    }


    private void getCareTakers() {

        careReceivers = new ArrayList<>();
        adapter = new CareGiverAdapter(this,R.layout.care_giver_adapter, careReceivers);
        careReceiverListView.setAdapter((ListAdapter) adapter);
        db.collection("Users").document(mAuth.getCurrentUser().getEmail()).collection("people").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User careGiver = new User(document.getString("name"), document.getString("phoneNum"), document.getId(), "Care Giver");
                                careReceivers.add(careGiver);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
            return;
        }
        else { Toast.makeText(getBaseContext(), "Press back again in order to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

}