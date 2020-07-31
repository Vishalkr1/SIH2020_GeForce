package com.example.hack2020;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        textView = findViewById(R.id.textView2);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            textView.setText(bundle.getString("Output"));
        }
    }
}