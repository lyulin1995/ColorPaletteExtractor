package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaletteActivity extends AppCompatActivity {

    private TextView txt1, txt2, txt3, txt4;
    Intent intent;
    String paletteId ;
    String imagePath;
    Bitmap imageBitmap;
    ImageView paletteImageView;
    private String TAG = "Palette Activity";
    StorageReference storageReference;
    Map<String, String> paletteObj = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);

        init();
        intent = getIntent();
        paletteId = intent.getStringExtra("paletteId");
        imagePath = intent.getStringExtra("imagePath");
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference ref = storageReference.child("pictures/" + imagePath);
        final long ONE_MEGABYTE = 1024 * 1024 * 5;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                createPalette(imageBitmap);
                paletteImageView.setImageBitmap(imageBitmap);
//                if (palette.get("dominantColor") != null) {
//                    paletteImageView.setBackgroundColor(Color.parseColor(palette.get("dominantColor")));
//                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void createPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if(vibrant!=null){
                    txt1.setBackgroundColor(vibrant.getRgb());
                    txt1.setTextColor(vibrant.getTitleTextColor());
                    txt1.setText("Vibrant");
                    String hex =  Integer.toHexString(vibrant.getRgb());
                    paletteObj.put("vibrantColor", hex);
                }

                Palette.Swatch lightVibrant = palette.getLightVibrantSwatch();
                if(lightVibrant!=null){
                    txt2.setBackgroundColor(lightVibrant.getRgb());
                    txt2.setTextColor(lightVibrant.getTitleTextColor());
                    txt2.setText("Light Vibrant");
                    String hex =  Integer.toHexString(lightVibrant.getRgb());
                    paletteObj.put("lightVibrantColor", hex);
                }

                Palette.Swatch dominant = palette.getDominantSwatch();
                if(dominant!=null){
                    txt3.setBackgroundColor(dominant.getRgb());
                    txt3.setTextColor(dominant.getTitleTextColor());
                    txt3.setText("Dominant");
                    String hex =  Integer.toHexString(dominant.getRgb());
                    paletteObj.put("dominantColor", hex);
                }

                Palette.Swatch darkMuted = palette.getDarkMutedSwatch();
                if(darkMuted!=null){
                    txt4.setBackgroundColor(darkMuted.getRgb());
                    txt4.setTextColor(darkMuted.getTitleTextColor());
                    txt4.setText("Dark Muted");
                    String hex =  Integer.toHexString(darkMuted.getRgb());
                    paletteObj.put("darkMutedColor", hex);
                }
            }
        });
    }

    private void init() {
        this.txt1 = findViewById(R.id.txt1);
        this.txt2 = findViewById(R.id.txt2);
        this.txt3 = findViewById(R.id.txt3);
        this.txt4 = findViewById(R.id.txt4);
        paletteImageView = findViewById(R.id.paletteImageView);

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