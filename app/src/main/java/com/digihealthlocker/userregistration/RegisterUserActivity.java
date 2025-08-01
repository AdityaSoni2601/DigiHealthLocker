package com.digihealthlocker.userregistration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.digihealthlocker.DashboardActivity;
import com.digihealthlocker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import Utility.AppHandler;
import Utility.Util;

public class RegisterUserActivity extends AppCompatActivity {

    private LinearLayout keywordsSet1, keywordsSet2, keywordsSet3;
    private AppHandler animationHandler;
    private Runnable animationRunnable;
    private int currentSet = 0;
    private TextInputEditText userFirstName, userMiddleName, userLastName, userPhoneOrEmail, userPassword, userConfirmPassword;
    private MaterialButton signUpButton;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_user_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userFirstName = findViewById(R.id.userFirstName);
        userMiddleName = findViewById(R.id.userMiddleName);
        userLastName = findViewById(R.id.userLastName);
        userPhoneOrEmail = findViewById(R.id.userPhoneOrEmail);
        userPassword = findViewById(R.id.userNewPassword);
        userConfirmPassword = findViewById(R.id.userConfirmNewPassword);
        signUpButton = findViewById(R.id.completeSignUpButton);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onResume(){
        super.onResume();
        initializeKeywordAnimation();

        signUpButton.setOnClickListener(v -> {
            String firstName = String.valueOf(userFirstName.getText()).trim();
            String middleName = String.valueOf(userMiddleName.getText()).trim();
            String lastName = String.valueOf(userLastName.getText()).trim();
            String emailOrPhone = "";
            emailOrPhone = String.valueOf(userPhoneOrEmail.getText()).trim();
            String newPassword = String.valueOf(userPassword.getText()).trim();
            String confirmNewPassword = String.valueOf(userConfirmPassword.getText()).trim();

            if(Util.isNullOrEmpty(firstName)) {
                Util.makeToast(RegisterUserActivity.this, "First name field cannot be empty!", 1);
            }
            if(Util.isNullOrEmpty(emailOrPhone)) {
                Util.makeToast(RegisterUserActivity.this, "E-mail or Phone field cannot be empty!", 1);
            }
            if(Util.isNullOrEmpty(newPassword)) {
                Util.makeToast(RegisterUserActivity.this, "New password field cannot be empty!", 1);
            }
            if(Util.isNullOrEmpty(confirmNewPassword) || !  newPassword.equals(confirmNewPassword)) {
                Util.makeToast(RegisterUserActivity.this, "Passwords do not match!", 1);
            }

            boolean isValidPhone = Util.isValidPhoneNumber(emailOrPhone);
            boolean isValidGmail = Util.isValidEmailAddress(emailOrPhone);
            if(!isValidGmail && !isValidPhone) {
                Util.makeToast(RegisterUserActivity.this, "Please enter valid Gmail ID or Phone Number!", 1);
            }

            if(isValidGmail) {
                signInWithGmail(emailOrPhone, newPassword);
            } else {
                signInWithPhone(emailOrPhone, newPassword);
            }
        });
    }

    private void initializeKeywordAnimation() {
        keywordsSet1 = findViewById(R.id.keywordsSet1);
        keywordsSet2 = findViewById(R.id.keywordsSet2);
        keywordsSet3 = findViewById(R.id.keywordsSet3);

        animationHandler = AppHandler.getInstance();
        startKeywordAnimation();
    }


    private void startKeywordAnimation() {
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                animateToNextSet();
                animationHandler.postDelayed(this, 4000);
            }
        };

        animationHandler.postDelayed(animationRunnable, 3000);
    }

    private void animateToNextSet() {
        LinearLayout currentLayout = getCurrentLayout();
        LinearLayout nextLayout = getNextLayout();

        ObjectAnimator slideOut = ObjectAnimator.ofFloat(currentLayout, "translationX", 0f, -1000f);
        slideOut.setDuration(500);
        slideOut.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(currentLayout, "alpha", 1f, 0f);
        fadeOut.setDuration(300);

        nextLayout.setTranslationX(1000f);
        nextLayout.setAlpha(0f);
        nextLayout.setVisibility(View.VISIBLE);

        ObjectAnimator slideIn = ObjectAnimator.ofFloat(nextLayout, "translationX", 1000f, 0f);
        slideIn.setDuration(500);
        slideIn.setInterpolator(new DecelerateInterpolator());
        slideIn.setStartDelay(200);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(nextLayout, "alpha", 0f, 1f);
        fadeIn.setDuration(400);
        fadeIn.setStartDelay(300);

        slideOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentLayout.setVisibility(View.INVISIBLE);
                currentLayout.setTranslationX(0f);
                currentLayout.setAlpha(1f);
            }
        });

        slideOut.start();
        fadeOut.start();
        slideIn.start();
        fadeIn.start();

        currentSet = (currentSet + 1) % 3;
    }

    private LinearLayout getCurrentLayout() {
        switch (currentSet) {
            case 1: return keywordsSet2;
            case 2: return keywordsSet3;
            default: return keywordsSet1;
        }
    }

    private LinearLayout getNextLayout() {
        int nextSet = (currentSet + 1) % 3;
        switch (nextSet) {
            case 1: return keywordsSet2;
            case 2: return keywordsSet3;
            default: return keywordsSet1;
        }
    }

    private void signInWithGmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Util.makeToast(RegisterUserActivity.this, "Registration successful!", 1);
                    startActivity(new Intent(RegisterUserActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Util.makeToast(RegisterUserActivity.this, "Registration failed! Please try again later", 1);
                }
            }
        });
    }

    private void signInWithPhone(String phoneNumber, String password) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null && animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
        }
    }
}