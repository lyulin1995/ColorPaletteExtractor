package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaletteActivity extends AppCompatActivity {

    private TextView txt1, txt2, txt3, txt4;
    EditText ptxt1;
    Intent intent;
    String paletteId ;
    String imagePath;
    Bitmap imageBitmap;
    ImageView paletteImageView;
    String paletteTitle;
    private String TAG = "Palette Activity";
    StorageReference storageReference;
    PaletteObj paletteObj;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);

        init();
        intent = getIntent();
        paletteId = intent.getStringExtra("paletteId");
        imagePath = intent.getStringExtra("imagePath");

        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }
        userRef = db.collection("user").document(uid);

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference ref = storageReference.child("pictures/" + imagePath);
        Log.d(TAG, "pictures/" + imagePath);
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

        ptxt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("edittext", s.toString());
                paletteTitle = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void createPalette(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                //Add a new palette object into firebase
                paletteObj = new PaletteObj("", imagePath);
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if(vibrant!=null){
                    txt1.setBackgroundColor(vibrant.getRgb());
                    txt1.setTextColor(vibrant.getTitleTextColor());
                    txt1.setText("Vibrant");
                    String hex =  Integer.toHexString(vibrant.getRgb());
                    paletteObj.setVibrantColor(hex);
                }

                Palette.Swatch lightVibrant = palette.getLightVibrantSwatch();
                if(lightVibrant!=null){
                    txt2.setBackgroundColor(lightVibrant.getRgb());
                    txt2.setTextColor(lightVibrant.getTitleTextColor());
                    txt2.setText("Light Vibrant");
                    String hex =  Integer.toHexString(lightVibrant.getRgb());
                    paletteObj.setLightVibrantColor(hex);
                }

                Palette.Swatch dominant = palette.getDominantSwatch();
                if(dominant!=null){
                    txt3.setBackgroundColor(dominant.getRgb());
                    txt3.setTextColor(dominant.getTitleTextColor());
                    txt3.setText("Dominant");
                    String hex =  Integer.toHexString(dominant.getRgb());
                    paletteObj.setDominantColor(hex);
                }

                Palette.Swatch darkMuted = palette.getDarkMutedSwatch();
                if(darkMuted!=null){
                    txt4.setBackgroundColor(darkMuted.getRgb());
                    txt4.setTextColor(darkMuted.getTitleTextColor());
                    txt4.setText("Dark Muted");
                    String hex =  Integer.toHexString(darkMuted.getRgb());
                    paletteObj.setDarkMutedColor(hex);
                }
            }
        });
        userRef.update("savedPalette", FieldValue.arrayUnion(paletteObj));
    }

    private void init() {
        this.txt1 = findViewById(R.id.txt1);
        this.txt2 = findViewById(R.id.txt2);
        this.txt3 = findViewById(R.id.txt3);
        this.txt4 = findViewById(R.id.txt4);
        paletteImageView = findViewById(R.id.paletteImageView);
        ptxt1 = findViewById(R.id.ptxt1);
        paletteTitle = ptxt1.getText().toString();
    }

    /** Called when the user taps the Edit button */
    public void onClickEdit(View view) {
        Intent intent = new Intent(this, EditPaletteActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Save button */
    public void onClickSave(View view) {
        paletteImageView.setDrawingCacheEnabled(true);
        paletteImageView.buildDrawingCache();
        Bitmap images = ((BitmapDrawable) paletteImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        images.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = storageReference.child("pictures/");

        StorageReference place = ref.child(imagePath);
        UploadTask uploadTask = place.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                int errorCode = ((StorageException) exception).getErrorCode();
                String errorMessage = exception.getMessage();
                Log.w(TAG, errorMessage);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                userRef.update("savedPalette", FieldValue.arrayUnion(paletteObj));
            }
        });
    }
}