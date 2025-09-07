package com.digihealthlocker;

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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import Utility.AppHandler;
import Utility.SharedPref;
import Utility.Util;
import Utility.AppConstants;

public class RegisterUserActivity extends AppCompatActivity {
    public interface UserRegistrationResult {
        void onUserRegisteredAwaitingVerification();
    }

    public interface UserEmailVerificationResult {
        void onEmailVerificationResult(boolean result);
    }

    public interface UserPhoneVerificationResult {
        void onPhoneVerificationResult(boolean result);
    }

    PhoneAuthProvider.ForceResendingToken mResendtoken = null;

    private LinearLayout keywordsSet1, keywordsSet2, keywordsSet3;
    private AppHandler animationHandler;
    private Runnable animationRunnable;
    private int currentSet = 0;
    private TextInputEditText userFirstName, userMiddleName, userLastName, userPhoneOrEmail, userNewPassword, userConfirmNewPassword;
    private MaterialButton signUpButton;
    FirebaseAuth mAuth;
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_MIDDLE_NAME = "middle_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL_OR_PHONE = "email_or_phone";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CONFIRM_PASSWORD = "confirm_password";
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_user_activity), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBarInsets.bottom);
            return insets;
        });

        userFirstName = findViewById(R.id.userFirstName);
        userMiddleName = findViewById(R.id.userMiddleName);
        userLastName = findViewById(R.id.userLastName);
        userPhoneOrEmail = findViewById(R.id.userPhoneOrEmail);
        userNewPassword = findViewById(R.id.userNewPassword);
        userConfirmNewPassword = findViewById(R.id.userConfirmNewPassword);
        signUpButton = findViewById(R.id.completeSignUpButton);

        mAuth = FirebaseAuth.getInstance();


        Bundle userDataBundle = getIntent().getExtras();
        if(userDataBundle != null) {
            updateFields(userDataBundle);
        } else if(savedInstanceState != null) {
            updateFields(savedInstanceState);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        initializeKeywordAnimation();

        signUpButton.setOnClickListener(v -> {
            String firstName = String.valueOf(userFirstName.getText()).trim();
            String middleName = String.valueOf(userMiddleName.getText()).trim();
            String lastName = String.valueOf(userLastName.getText()).trim();
            String emailOrPhone = String.valueOf(userPhoneOrEmail.getText()).trim();
            String newPassword = String.valueOf(userNewPassword.getText()).trim();
            String confirmNewPassword = String.valueOf(userConfirmNewPassword.getText()).trim();

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

            Bundle dataBundle = new Bundle();
            dataBundle.putString(AppConstants.USER_FIRST_NAME_KEY, firstName);
            dataBundle.putString(AppConstants.USER_MIDDLE_NAME_KEY, middleName);
            dataBundle.putString(AppConstants.USER_LAST_NAME_KEY, lastName);
            dataBundle.putString(AppConstants.USER_PHONE_OR_EMAIL_KEY, emailOrPhone);
            dataBundle.putString(AppConstants.USER_PASSWORD_KEY, newPassword);
            dataBundle.putString(AppConstants.USER_CONFIRM_PASSWORD_KEY, confirmNewPassword);

            if(isValidGmail) {
                createUserWithEmail(emailOrPhone, newPassword, new UserRegistrationResult() {
                    @Override
                    public void onUserRegisteredAwaitingVerification() {
                        VerifyEmailBottomSheet sheet = new VerifyEmailBottomSheet(dataBundle, new UserEmailVerificationResult() {
                            @Override
                            public void onEmailVerificationResult(boolean result) {
                                if(result) {
                                    SharedPref.getInstance().setUserFirstName(firstName);
                                    if (!Util.isNullOrEmpty(middleName)) {
                                        SharedPref.getInstance().setUserMiddleName(middleName);
                                    }
                                    if (!Util.isNullOrEmpty(lastName)) {
                                        SharedPref.getInstance().setUserMiddleName(lastName);
                                    }
                                    SharedPref.getInstance().setUserEmail(emailOrPhone);
                                    AskPassKeyCreationBottomSheet askPassKeyCreationBottomSheet = new AskPassKeyCreationBottomSheet();
                                    askPassKeyCreationBottomSheet.show(getSupportFragmentManager(), "AskPassKeyCreationBottomSheet");
                                }
                            }
                        });
                        sheet.show(getSupportFragmentManager(), "VerifyEmailBottomSheet");
                    }
                });

            } else {
                String phoneNumber = "+91".concat(emailOrPhone);
                createUserWithPhone(phoneNumber, newPassword, new UserRegistrationResult() {
                    @Override
                    public void onUserRegisteredAwaitingVerification() {
                        // This callback is triggered after the OTP is sent.
                        // Now, show a bottom sheet or dialog to get the OTP from the user.
                        // We pass the mVerificationId to the sheet so it can verify the OTP.
                        VerifyPhoneBottomSheet sheet = new VerifyPhoneBottomSheet(mVerificationId, mResendtoken, dataBundle, new UserPhoneVerificationResult() {
                            @Override
                            public void onPhoneVerificationResult(boolean result) {
                                if (result) {
                                    // This logic runs upon successful phone verification
                                    SharedPref.getInstance().setUserFirstName(firstName);
                                    if (!Util.isNullOrEmpty(middleName)) {
                                        SharedPref.getInstance().setUserMiddleName(middleName);
                                    }
                                    if (!Util.isNullOrEmpty(lastName)) {
                                        // NOTE: Corrected a bug from the original snippet.
                                        // The original used setUserMiddleName for the last name.
                                        SharedPref.getInstance().setUserLastName(lastName);
                                    }
                                    SharedPref.getInstance().setUserPhone(emailOrPhone);
                                    startActivity(new Intent(RegisterUserActivity.this, DashboardActivity.class));
                                    finish();
                                }
                            }
                        });
                        sheet.show(getSupportFragmentManager(), "VerifyPhoneOtpBottomSheet");
                    }
                });
            }
        });

    }

    private void updateFields(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        userFirstName.setText(savedInstanceState.getString(AppConstants.USER_FIRST_NAME_KEY, ""));
        userMiddleName.setText(savedInstanceState.getString(AppConstants.USER_MIDDLE_NAME_KEY, ""));
        userLastName.setText(savedInstanceState.getString(AppConstants.USER_LAST_NAME_KEY, ""));
        userPhoneOrEmail.setText(savedInstanceState.getString(AppConstants.USER_PHONE_OR_EMAIL_KEY, ""));
        userNewPassword.setText(savedInstanceState.getString(AppConstants.USER_PASSWORD_KEY, ""));
        userConfirmNewPassword.setText(savedInstanceState.getString(AppConstants.USER_CONFIRM_PASSWORD_KEY, ""));
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppConstants.USER_FIRST_NAME_KEY, String.valueOf(userFirstName.getText()));
        outState.putString(AppConstants.USER_MIDDLE_NAME_KEY, String.valueOf(userMiddleName.getText()));
        outState.putString(AppConstants.USER_LAST_NAME_KEY, String.valueOf(userLastName.getText()));
        outState.putString(AppConstants.USER_PHONE_OR_EMAIL_KEY, String.valueOf(userPhoneOrEmail.getText()));
        outState.putString(AppConstants.USER_PASSWORD_KEY, String.valueOf(userNewPassword.getText()));
        outState.putString(AppConstants.USER_CONFIRM_PASSWORD_KEY, String.valueOf(userConfirmNewPassword.getText()));
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

    private void createUserWithEmail(String email, String password, UserRegistrationResult callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("FirebaseSuccess", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(sendTask -> {
                                            if (sendTask.isSuccessful()) {
                                                Log.d("FirebaseSuccess", "Verification email sent.");
                                                callback.onUserRegisteredAwaitingVerification();
                                            } else {
                                                Exception exception = sendTask.getException();
                                                Log.w("FirebaseError", "sendEmailVerification", exception);
                                                String errorMessage = exception != null ? exception.getMessage() : "Failed to send verification email.";
                                                Util.makeToast(RegisterUserActivity.this, "Error: " + errorMessage, 1);
                                            }
                                        });
                            }
                        } else {
                            Exception exception = task.getException();
                            Log.w("FirebaseError", "createUserWithEmail:failure", exception);
                            String errorMessage = exception != null ? exception.getMessage() : "Authentication failed.";
                            Util.makeToast(RegisterUserActivity.this, "Error: " + errorMessage, 1);
                        }
                    }
                });
    }


    private void createUserWithPhone(String phoneNumber, String password, UserRegistrationResult callback) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number.
                // We save the verification ID and token to use later.
                Log.d("FirebaseSuccess", "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendtoken = token;
                // Trigger the UI to ask the user for the OTP.

                callback.onUserRegisteredAwaitingVerification();
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback is invoked in two situations:
                // 1. Instant verification without user action.
                // 2. Auto-retrieval of the SMS code on some devices.
                Log.d("FirebaseSuccess", "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential); // Directly sign in the user
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked for invalid requests or other errors.
                Log.w("FirebaseError", "onVerificationFailed", e);
                String errorMessage = e.getMessage();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    errorMessage = "Invalid phone number format.";
                }
                Util.makeToast(RegisterUserActivity.this, "Error: " + errorMessage, 1);
            }
        };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callbacks)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Signs in the user with the given PhoneAuthCredential.
     * This can be called either on auto-retrieval (onVerificationCompleted) or after
     * the user manually enters the OTP.
     * @param credential The credential from Firebase.
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseSuccess", "User signed in successfully.");

                    } else {
                        Exception exception = task.getException();
                        Log.w("FirebaseError", "signInWithCredential:failure", exception);
                        String errorMessage = "Authentication failed.";
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "The verification code is invalid.";
                        }
                        Util.makeToast(RegisterUserActivity.this, "Error: " + errorMessage, 1);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null && animationRunnable != null) {
            animationHandler.removeCallbacks(animationRunnable);
        }
    }
}