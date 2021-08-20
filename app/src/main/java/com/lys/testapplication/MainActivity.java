package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String TAG = "Main Activity";
    TextView verifyMsg;
    Button verifyEmailBtn, savedPaletteBtn;
    AlertDialog.Builder reset_alert;
    LayoutInflater inflater;
    FirebaseAuth auth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get instance of firebase firestore
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String path;
    StorageReference storageRef = storage.getReference();
    StorageReference picRef;

    DocumentReference userRef;
    private String uID;
    // Global variables
    Map<String, Object> user = new HashMap<>();
    List<String> listUploads;
    ImageView preview;;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        Button logout = findViewById(R.id.logoutBtn);
        verifyMsg = findViewById(R.id.verifyEmailMsg);
        verifyEmailBtn = findViewById(R.id.verifyEmailBtn);
        savedPaletteBtn = findViewById(R.id.savedPaletteBtn);

        reset_alert = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();

        preview = findViewById(R.id.palettePreview);
        preview.buildDrawingCache();

        // Get the userID from the current user
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uID = profile.getUid();
        }
        userRef = db.collection("user").document(uID);

        if (!auth.getCurrentUser().isEmailVerified()){
            verifyEmailBtn.setVisibility(View.VISIBLE);
            verifyMsg.setVisibility(View.VISIBLE);
        }

        savedPaletteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SavePaletteActivity.class));
            }
        });

        verifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send verification email
                auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                        verifyEmailBtn.setVisibility(View.GONE);
                        verifyMsg.setVisibility(View.GONE);
                    }
                });
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });


        // Get user info to change welcome message, about request message and query.
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    // Set user as user document in Firebase
                    user = document.getData();
                    // Get important information
                    if (user != null) {
                        listUploads = (List<String>) user.get("uploads");
                        path = listUploads.get(0);
                        String fullPath = "pictures/" + uID  + path;
                        picRef = storageRef.child(fullPath);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        picRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                preview.setImageBitmap(picture);
//                                preview.setId(0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                            }
                        });
                        Log.d(TAG, listUploads.toString());
                    }
                }
                else {
                    Log.d(TAG, "get failed with: ", task.getException());
                }
            }
        });
    }

    /** Called when the user taps the Camera button */
    public void onClickUpload(View view) {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps a saved palette */
    public void onClickSavedPalette(View view) {
//        int vID = view.getId();
//        String paletteId = listUploads.get(vID);
        Intent intent = new Intent(this, PaletteActivity.class);
//        intent.putExtra("paletteId", paletteId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resetUserPassword){
            startActivity(new Intent(getApplicationContext(), ResetPassword.class));
        }

        if (item.getItemId() == R.id.updateEmailMenu){
            View view = inflater.inflate(R.layout.reset_pop, null);
            reset_alert.setTitle("Update Email").setMessage("Enter New Email Address.")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // validate the email address
                            EditText email = view.findViewById(R.id.reset_email_pop);
                            if (email.getText().toString().isEmpty()){
                                email.setError("Required Field");
                                return;
                            }
                            // send the reset link
                            FirebaseUser user = auth.getCurrentUser();
                            user.updateEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }).setNegativeButton("Cancel", null)
                    .setView(view)
                    .create().show();

        }

        if (item.getItemId() == R.id.delete_account_menu){
            reset_alert.setTitle("Delete Account Permanently ?")
                    .setMessage("Are You Sure?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseUser user = auth.getCurrentUser();
                            user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }).setNegativeButton("Cancel", null)
                    .create().show();
        }


        return super.onOptionsItemSelected(item);

    }
}