package com.lys.testapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    EditText registerFullName, registerEmail, registerPassword, registerConfPass;
    Button registerUserBtn, gotoLogin;
    FirebaseAuth fAuth;

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

                // Store the email and password
                fAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
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