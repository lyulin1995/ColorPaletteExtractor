package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private static final String TAG = "" ;
    EditText registerFullName, registerEmail, registerPassword, registerConfPass;
    Button registerUserBtn, gotoLogin;
    FirebaseAuth fAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFullName = findViewById(R.id.registerFullName);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfPass = findViewById(R.id.confPassword);
        registerUserBtn = findViewById(R.id.registerBtn);
        gotoLogin = findViewById(R.id.gotoLogin);

        fAuth = FirebaseAuth.getInstance();

        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        registerUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // extract the data from the form
                String fullName = registerFullName.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confPass = registerConfPass.getText().toString();

                if (fullName.isEmpty()){
                    registerFullName.setError("Full Name is Required");
                    return;
                }

                if (email.isEmpty()){
                    registerEmail.setError("Email is Required");
                    return;
                }

                if (password.isEmpty()){
                    registerPassword.setError("Password is Required");
                    return;
                }

                if (confPass.isEmpty()){
                    registerConfPass.setError("Confirm Password is Required");
                    return;
                }

                if (!password.equals(confPass)){
                    registerConfPass.setError("Password Do not match");
                    return;
                }

                // data is validated
                // register the user using firebase
                Toast.makeText(Register.this, "Data Validated", Toast.LENGTH_SHORT).show();
                user.put("fullname", fullName);
                user.put("email", email);
                user.put("password", password);

                // Add a new document with a generated ID
                db.collection("user")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });

                // Store the email and password
                fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Creates new firebase user document with proper attributes and auth ID as document id
                        db.collection("user").document(email).set(user);
                        // send user to next page
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();  // don't want user come back registration activity

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });





            }
        });

    }
}