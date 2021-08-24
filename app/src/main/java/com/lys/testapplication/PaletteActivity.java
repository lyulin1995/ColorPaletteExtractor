package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class PaletteActivity extends AppCompatActivity {

    private TextView txt1, txt2, txt3, txt4;
    EditText titleEditText;
    Intent intent;
    String imagePath;
    Bitmap imageBitmap;
    ImageView paletteImageView;
    String paletteTitle;
    private String TAG = "Palette Activity";
    StorageReference storageReference;
    PaletteObj paletteObj;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    CollectionReference paletteCollectionRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette);

        init();
        intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        paletteCollectionRef = db.collection("palette");

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

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

                Palette.Swatch color_1 = palette.getDarkVibrantSwatch();
                if(color_1!=null){
                    txt1.setBackgroundColor(color_1.getRgb());
                    txt1.setTextColor(color_1.getTitleTextColor());
                    txt1.setText("Color 1");
                    String hex =  Integer.toHexString(color_1.getRgb());
                    paletteObj.setColorOne(hex);
                }

                Palette.Swatch color_2 = palette.getLightVibrantSwatch();
                if(color_2!=null){
                    txt2.setBackgroundColor(color_2.getRgb());
                    txt2.setTextColor(color_2.getTitleTextColor());
                    txt2.setText("Color 2");
                    String hex =  Integer.toHexString(color_2.getRgb());
                    paletteObj.setColorTwo(hex);
                }

                Palette.Swatch color_3 = palette.getLightMutedSwatch();
                if(color_3!=null){
                    txt3.setBackgroundColor(color_3.getRgb());
                    txt3.setTextColor(color_3.getTitleTextColor());
                    txt3.setText("Color 3");
                    String hex =  Integer.toHexString(color_3.getRgb());
                    paletteObj.setColorThree(hex);
                }

                Palette.Swatch color_4 = palette.getDarkMutedSwatch();
                if(color_4!=null){
                    txt4.setBackgroundColor(color_4.getRgb());
                    txt4.setTextColor(color_4.getTitleTextColor());
                    txt4.setText("Color 4");
                    String hex =  Integer.toHexString(color_4.getRgb());
                    paletteObj.setColorFour(hex);
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
        titleEditText = findViewById(R.id.ptxt1);
        paletteTitle = titleEditText.getText().toString();
    }

    /** Called when the user taps the Save button */
    public void onClickSave(View view) {
        paletteImageView.setDrawingCacheEnabled(true);
        paletteImageView.buildDrawingCache();
        Bitmap images = ((BitmapDrawable) paletteImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        images.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        paletteObj.setTitle(paletteTitle);

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
                userRef.update("savedPalette", FieldValue.arrayUnion( paletteObj));
                Intent intent = new Intent(getApplicationContext(), SavedPaletteActivity.class);
                startActivity(intent);
            }
        });
    }
}