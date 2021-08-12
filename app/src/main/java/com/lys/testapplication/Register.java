package com.lys.testapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends AppCompatActivity {

    EditText registerFullName, registerEmail, registerPassword, registerConfPass;
    Button registerUserBtn, gotoLogin;

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



            }
        });

    }
}