package com.lys.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Details extends AppCompatActivity {
    // Receiving data in details
    TextView textTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // set back button

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textTitle = findViewById(R.id.detailTitle);

        Intent i = getIntent();
        String title = i.getStringExtra("title");
        textTitle.setText(title);
    }
}