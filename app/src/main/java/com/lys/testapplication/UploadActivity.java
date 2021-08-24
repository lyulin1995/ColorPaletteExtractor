package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    public static final int CAMERA_REQUEST_CODE = 1;
    public static final int GALLERY_REQUEST_CODE = 2;
    private String TAG = "UploadActivity";
    String currentPhotoPath;
    ImageView preview;
    String fullPath;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    StorageReference storageReference;
    private String imageName = "";

    private String uid;
    Map<String, Object> path = new HashMap<>();

    Button generateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        generateBtn = findViewById(R.id.generateBtn);

        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }
        preview = findViewById(R.id.uploadImageView);
        userRef = db.collection("user").document(uid);
        // initialize the storage difference
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    //allows a user to upload a photo directly taken from their camera
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickCamera(View v){
        // Check if the permissions are granted.
        // If one is not granted, request it
        if ((ContextCompat.checkSelfPermission(
                UploadActivity.this, Manifest.permission.CAMERA ) !=
                PackageManager.PERMISSION_GRANTED)) {
            Log.e(TAG, "Not granted");
            requestPermissions(
                    new String[] { Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        }
        // If the permissions are granted and submitByCamera is true,
        // i.e. we want to submit by camera, we create a new intent for this.
        if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if ((getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) && (ContextCompat.checkSelfPermission(
                    UploadActivity.this, Manifest.permission.CAMERA ) ==
                    PackageManager.PERMISSION_GRANTED )) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.lys.android.fileProvider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        }
    }

    public void onClickGallery (View v) {
        if ((ContextCompat.checkSelfPermission(
                UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) !=
                PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(
                UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE ) !=
                PackageManager.PERMISSION_GRANTED)) {
            Log.e(TAG, "Not granted");
            requestPermissions(
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    GALLERY_REQUEST_CODE);
        } else {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        }
    }


    //auxiliary method for cameraSubmit to create the path of a new image file
    // The path is created based on the current date to avoid duplicate names in the database
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //is called automatically after the intent(open gallery or open camera) is finished
    //sets the image to the preview on the activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the request was successful
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // If the image was taken by camera, we need to do some extra work
                BitmapFactory.Options bitMapOption = new BitmapFactory.Options();
                bitMapOption.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(currentPhotoPath, bitMapOption);
                int imageWidth = bitMapOption.outWidth;
                int imageHeight = bitMapOption.outHeight;
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(currentPhotoPath), imageWidth, imageHeight);
                preview.setImageBitmap(thumbImage);
                if (thumbImage != null) {
                    generateBtn.setEnabled(true);
                }
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                preview.setImageURI(selectedImage);
                generateBtn.setEnabled(true);
            }
        }


    }

    //uploads the file to firestore and adds the path as well as submission details to the firebase
    //record
    public void onClickGenerate(View v) {
        preview.setDrawingCacheEnabled(true);
        preview.buildDrawingCache();
        Bitmap images = ((BitmapDrawable) preview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        images.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = storageReference.child("pictures/");
        fullPath = uid + "/" + imageName + ".jpg";
        path.put("path", fullPath);

        StorageReference place = ref.child(fullPath);
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
                Toast.makeText(UploadActivity.this, "A photo was uploaded", Toast.LENGTH_SHORT).show();
                Intent paletteActivity = new Intent(UploadActivity.this, PaletteActivity.class);
                Log.d(TAG, fullPath);
                paletteActivity.putExtra("imagePath", fullPath);
                startActivity(paletteActivity);
            }
        });
    }
}