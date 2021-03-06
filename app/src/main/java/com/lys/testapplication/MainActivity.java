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

        // Get the userID from the current user
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uID = profile.getUid();
        }
        userRef = db.collection("user").document(uID);

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
                    }
                }
                else {
                    Log.d(TAG, "get failed with: ", task.getException());
                }
            }
        });
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

        if (item.getItemId() == R.id.logout_menu){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }


        return super.onOptionsItemSelected(item);

    }
}