package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class Details extends AppCompatActivity {
    // Receiving data in details
    TextView textTitle;
    Map<String, Object>  paletteObj;

    private TextView textView1, textView2, textView3, textView4;
    TextView titleEditText;
    String title;
    String imagePath;
    Map<String, String> paletteDetail;

    Bitmap imageBitmap;
    ImageView paletteImageView;
    private String TAG = "Palette Activity";
    StorageReference storageReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    private String uid;

    ClipData clip;
    ClipboardManager clipboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textTitle = findViewById(R.id.paletteDetailTitle);

        Intent intent = getIntent();
        paletteObj = (Map<String, Object>)intent.getSerializableExtra("paletteObj");
        title = paletteObj.get("title").toString();
        paletteDetail = (Map<String, String>) paletteObj.get("paletteDetail");

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        init();
        textTitle.setText(title);
        imagePath = paletteObj.get("imagePath").toString();

        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }

        userRef = db.collection("user").document(uid);
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference ref = storageReference.child("pictures/" + imagePath);

        final long ONE_MEGABYTE = 1024 * 1024 * 5;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                setPalette();
                paletteImageView.setImageBitmap(imageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void setPalette() {
        String Color_1 = paletteDetail.get("color_1");
        int intColor_1 =(int) Long.parseLong(Color_1, 16);
        if (Color_1 != null) {
            textView1.setBackgroundColor(intColor_1);
            textView1.setText("Color 1");
            if (intColor_1 == 0000) {
                textView1.setText("No color is extracted");
            }
        }

        String Color_2 = paletteDetail.get("color_2");
        int intColor_2 =(int) Long.parseLong(Color_2, 16);
        if (Color_2 != null) {
            textView2.setBackgroundColor(intColor_2);
            textView2.setText("Color 2");
            if (intColor_2 == 0000) {
                textView2.setText("No color is extracted");
            }
        }

        String Color_3 = paletteDetail.get("color_3");
        int intColor_3 =(int) Long.parseLong(Color_3, 16);
        if (Color_3 != null) {
            textView3.setBackgroundColor(intColor_3);
            textView3.setText("Color 3");
            if (intColor_3 == 0000) {
                textView3.setText("No color is extracted");
            }
        }

        String Color_4 = paletteDetail.get("color_4");
        int intColor_4 =(int) Long.parseLong(Color_4, 16);
        if (Color_4 != null) {
            textView4.setBackgroundColor(intColor_4);
            textView4.setText("Color 4");
            if (intColor_4 == 0000) {
                textView4.setText("No color is extracted");
            }
        }

    }


    private void init() {
        this.textView1 = findViewById(R.id.colorOneTextView);
        this.textView2 = findViewById(R.id.colorTwoTextView);
        this.textView3 = findViewById(R.id.colorThreeTextView);
        this.textView4 = findViewById(R.id.colorFourTextView);
        paletteImageView = findViewById(R.id.paletteDetailImageView);
        titleEditText = findViewById(R.id.paletteDetailTitle);
    }

    public void onClickCopy (View v) {
        switch (v.getId()) {
            case R.id.colorOneTextView:
                // add your code
                clip = ClipData.newPlainText("copiedColor", paletteDetail.get("color_1"));
                clipboard.setPrimaryClip(clip);
                break;

            case R.id.colorTwoTextView:
                // add your code
                clip = ClipData.newPlainText("copiedColor", paletteDetail.get("color_2"));
                clipboard.setPrimaryClip(clip);
                break;

            case R.id.colorThreeTextView:
                // add your code
                clip = ClipData.newPlainText("copiedColor", paletteDetail.get("color_3"));
                clipboard.setPrimaryClip(clip);
                break;

            case R.id.colorFourTextView:
                // add your code
                clip = ClipData.newPlainText("copiedColor", paletteDetail.get("color_4"));
                clipboard.setPrimaryClip(clip);
                break;
            default:
                break;
        }
    }
}