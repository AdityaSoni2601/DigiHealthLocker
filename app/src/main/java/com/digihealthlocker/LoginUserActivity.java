package com.digihealthlocker;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginUserActivity extends AppCompatActivity {

    Button loginWithGoogleBtn, loginWithPhoneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_user_activity), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBarInsets.bottom);
            return insets;
        });

        loginWithGoogleBtn = findViewById(R.id.loginWithGoogleBtn);
        loginWithPhoneBtn = findViewById(R.id.loginWithPhoneBtn);

        loginWithGoogleBtn.setOnClickListener(v -> {
            // Handle Google login logic here
        });

        loginWithPhoneBtn.setOnClickListener(v -> {
            // Handle phone login logic here
        });
    }
}