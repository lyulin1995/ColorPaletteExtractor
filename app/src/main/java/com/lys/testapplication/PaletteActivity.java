package com.lys.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PaletteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);
    }
    /** Called when the user taps the Edit button */
    public void onClickEdit(View view) {
        Intent intent = new Intent(this, EditPaletteActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Save button */
    public void onClickSave(View view) {
        Intent intent = new Intent(this, SavePaletteActivity.class);
        startActivity(intent);
    }
}