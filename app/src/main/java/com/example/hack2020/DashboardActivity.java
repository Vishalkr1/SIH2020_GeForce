package com.example.hack2020;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "Sample";
    TextView textView;
    Button btn;
    public Bundle bundle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        textView = findViewById(R.id.textView2);
        btn = findViewById(R.id.retreiveMap);
        bundle = getIntent().getExtras();
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