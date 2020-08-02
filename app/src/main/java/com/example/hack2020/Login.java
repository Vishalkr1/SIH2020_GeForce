package com.example.hack2020;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextName, editTextEmail, editTextPassword, editTextPhone,editTextConfrimPassword;
    private Button login;
    private Spinner userTypeSpinner;
    private String userType;
    String TAG = "Sample";



    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.android.gms.location.LocationListener listener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextName = findViewById(R.id.et_name);
        editTextEmail = findViewById(R.id.et_email);
        editTextPassword = findViewById(R.id.et_password);
        editTextPhone = findViewById(R.id.phone);
        login = findViewById(R.id.button);
        editTextConfrimPassword = findViewById(R.id.cnfm_password);
        userTypeSpinner = findViewById(R.id.user_type);


        mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();


        findViewById(R.id.button).setOnClickListener(this);

        findViewById(R.id.btn_register).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                registerUser();
                break;
            case R.id.button:
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }


    private void registerUser() {
        final String name = editTextName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String cnf_passwrd = editTextConfrimPassword.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String userType = userTypeSpinner.getSelectedItem().toString().trim();

        boolean error = false;

        if (name.isEmpty()) {
            error = true;
            editTextName.setError(getString(R.string.input_error_name));
            editTextName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            error = true;
            editTextEmail.setError(getString(R.string.input_error_email));
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = true;
            editTextEmail.setError(getString(R.string.input_error_email_invalid));
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            error = true;
            editTextPassword.setError(getString(R.string.input_error_password));
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            error = true;
            editTextPassword.setError(getString(R.string.input_error_password_length));
            editTextPassword.requestFocus();
            return;
        }

        if (cnf_passwrd.isEmpty()) {
            error = true;
            editTextConfrimPassword.setError(getString(R.string.input_error_password));
            editTextConfrimPassword.requestFocus();
            return;
        }

        if (cnf_passwrd.length() < 6) {
            error = true;
            editTextConfrimPassword.setError(getString(R.string.input_error_password_length));
            editTextConfrimPassword.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(cnf_passwrd))
        {
            if(password.equals(cnf_passwrd) == false)
            {
                error = true;
                Toast.makeText(Login.this,"Password Does not Match",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (phone.isEmpty()) {
            error = true;
            editTextPhone.setError(getString(R.string.input_error_phone));
            editTextPhone.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            error = true;
            editTextPhone.setError(getString(R.string.input_error_phone_invalid));
            editTextPhone.requestFocus();
            return;
        }

        if (userType.equals("Choose a role")){
            error = true;
            findViewById(R.id.invalid_role).setVisibility(View.VISIBLE);
        }

            if(!error)
            {
                final FirebaseAuth firebaseAuth =  FirebaseAuth.getInstance();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If email is valid and not in use
                                if (task.isSuccessful()) {
                                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Login.this,"Logeed in",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    String userId = mAuth.getCurrentUser().getUid();
                                    UserInformation user = new UserInformation(name, email, userType, userId);
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");

                                    FirebaseUser CurrentUser = mAuth.getCurrentUser();
                                    // Creates a new user in the database
                                    HashMap<String, String> data = new HashMap<>();
                                    data.put("email", email);
                                    data.put("userID", userId);
                                    data.put("name",name);
                                    data.put("userType",userType);
                                    data.put("phoneNum",phone);
                                    CollectionReference collectionReference = db.collection("Users");
                                    collectionReference
                                            .document(email)
                                            .set(data)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Data Addition Successful");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Failure to add user to database
                                                    Log.d(TAG, "Data Addition Failed" + e.toString());
                                                }
                                            });
                                    Intent intent = new Intent(Login.this, IntermediateActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                }

                                // ...
                            }
                        });
        }


    }

}
