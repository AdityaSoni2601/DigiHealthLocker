package com.digihealthlocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import Utility.Util;

public class UserAuthenticationActivity extends AppCompatActivity {

    EditText userName, userPassword;
    Button signInBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_authentication);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_authentication_activity), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBarInsets.bottom);
            return insets;
        });

        userName = findViewById(R.id.userEmailOrPhoneNumber);
        userPassword = findViewById(R.id.userPassword);
        signInBtn = findViewById(R.id.signInBtn);

        mAuth = FirebaseAuth.getInstance();

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userName.getText().toString();
                String password = userPassword.getText().toString();
                if (Util.isNullOrEmpty(username) || Util.isNullOrEmpty(password)) {
                    Util.makeToast(UserAuthenticationActivity.this, "Please enter both username and password", Toast.LENGTH_LONG);
                }
                boolean isPhoneNumberValid = Util.isValidPhoneNumber(username);
                boolean isEmailValid = Util.isValidEmailAddress(username);
                if(!isPhoneNumberValid || !isEmailValid) {
                    Util.makeToast(UserAuthenticationActivity.this, "Please enter a valid email or phone number", Toast.LENGTH_LONG);
                }

                if (isPhoneNumberValid) {
//                    validatePhoneNumberAndPassword(username, password);
                } else {
                    validateEmailAndPassword(username, password);
                }
            }
        });
    }

    private void validateEmailAndPassword(String emailAddress, String password) {
        try {
            mAuth.signInWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}