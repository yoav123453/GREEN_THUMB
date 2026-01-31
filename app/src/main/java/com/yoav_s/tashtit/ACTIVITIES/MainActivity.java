package com.yoav_s.tashtit.ACTIVITIES;

import android.os.Bundle;
import android.widget.TextView;

import com.yoav_s.tashtit.R;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;

public class MainActivity extends BaseActivity {
    private TextView tvUpdateDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);
        //getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));
        setLayout(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        displayUpdateDate();

        initializeActivity();
    }

    private void displayUpdateDate() {
        tvUpdateDate = findViewById(R.id.tvUpdateDate);
        tvUpdateDate.setText("Update: " + updateDate);
    }

    @Override
    protected void initializeActivity() {
        initializeViews();
    }


    @Override
    protected void initializeViews() {

        setListeners();
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void setViewModel() {

    }
}