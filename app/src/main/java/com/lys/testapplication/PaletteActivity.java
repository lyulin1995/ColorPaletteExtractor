package com.lys.testapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PaletteActivity extends AppCompatActivity {

    private TextView txt1, txt2, txt3, txt4, txt5, txt6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);

        init();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sworm);

        crearPalette(bitmap);

    }

    private void crearPalette(Bitmap bitmap) {

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();

                if(vibrant!=null){
                    txt1.setBackgroundColor(vibrant.getRgb());
                    txt1.setTextColor(vibrant.getTitleTextColor());
                    txt1.setText("Vibrant");
                }

                Palette.Swatch muted = palette.getMutedSwatch();


                if(muted!=null){
                    txt2.setBackgroundColor(muted.getRgb());
                    txt2.setTextColor(muted.getTitleTextColor());
                    txt2.setText("Muted");
                }

                Palette.Swatch dominant = palette.getDominantSwatch();

                if(dominant!=null){
                    txt3.setBackgroundColor(dominant.getRgb());
                    txt3.setTextColor(dominant.getTitleTextColor());
                    txt3.setText("Dominant");
                }

                Palette.Swatch darkMuted = palette.getDarkMutedSwatch();

                if(darkMuted!=null){
                    txt4.setBackgroundColor(darkMuted.getRgb());
                    txt4.setTextColor(darkMuted.getTitleTextColor());
                    txt4.setText("Dark Muted");
                }

                Palette.Swatch lightMuted = palette.getLightMutedSwatch();

                if(lightMuted!=null){
                    txt5.setBackgroundColor(lightMuted.getRgb());
                    txt5.setTextColor(lightMuted.getTitleTextColor());
                    txt5.setText("Light Muted");
                }

                Palette.Swatch lightVibrant = palette.getLightVibrantSwatch();

                if(lightVibrant!=null){
                    txt6.setBackgroundColor(lightVibrant.getRgb());
                    txt6.setTextColor(lightVibrant.getTitleTextColor());
                    txt6.setText("Light Vibrant");
                }
            }
        });
    }

    private void init() {
        this.txt1 = findViewById(R.id.txt1);
        this.txt2 = findViewById(R.id.txt2);
        this.txt3 = findViewById(R.id.txt3);
        this.txt4 = findViewById(R.id.txt4);
        this.txt5 = findViewById(R.id.txt5);
        this.txt6 = findViewById(R.id.txt6);

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