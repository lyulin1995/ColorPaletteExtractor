package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.Toast;

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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE =1;
    private boolean submitByCamera = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String TAG = "UploadActivity";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String currentPhotoPath;
    ImageView preview;
    String fullPath;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userRef;
    StorageReference storageReference;
    private String imageName = "";

    private String uid;
    private String requestID;
    Map<String, Object> path = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }
        preview = findViewById(R.id.uploadImageView);
        requestID = getIntent().getStringExtra("RequestID");
        userRef = db.collection("user").document(uid);
        // initialize the storage difference
        storageReference = FirebaseStorage.getInstance().getReference();

    }

    //allows a user to upload a photo directly taken from their camera
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickCamera(View v){
        // Check if the permissions are granted.
        // If one is not granted, request it
        if (ContextCompat.checkSelfPermission(
                UploadActivity.this, Manifest.permission.CAMERA ) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[] { Manifest.permission.CAMERA},
                    1);
        }
        // If the permissions are granted and submitByCamera is true,
        // i.e. we want to submit by camera, we create a new intent for this.
        if (ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            submitByCamera = true;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) {
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
                    Log.d(TAG, photoURI.toString());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    public void onClickGallery (View v) {

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
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            // If the image was taken by camera, we need to do some extra work
            if (submitByCamera){
                BitmapFactory.Options bitMapOption=new BitmapFactory.Options();
                bitMapOption.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(currentPhotoPath, bitMapOption);
                int imageWidth=bitMapOption.outWidth;
                int imageHeight=bitMapOption.outHeight;
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(currentPhotoPath), imageWidth, imageHeight);
                preview.setImageBitmap(thumbImage);
            }else if (data != null){
                Uri selectedImage = data.getData();
                preview.setImageURI(selectedImage);
            }
        }

    }

    //uploads the file to firestore and adds the path as well as submission details to the firebase
    //record
    public void onClickSubmit(View v){
        preview.setDrawingCacheEnabled(true);
        preview.buildDrawingCache();
        Bitmap images = ((BitmapDrawable) preview.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        images.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                db.collection("path")
                        .add(path)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                //update the values in firebase
                                userRef.update("uploads", FieldValue.arrayUnion(fullPath));

                                Toast.makeText(UploadActivity.this, "A photo was uploaded", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(UploadActivity.this, MainActivity.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed to upload :( ", e);
                            }
                        });
            }
        });
    }
}