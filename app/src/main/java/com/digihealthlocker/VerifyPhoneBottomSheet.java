package com.digihealthlocker;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import Utility.Util;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Locale;


public class VerifyPhoneBottomSheet extends BottomSheetDialogFragment {
    private FirebaseAuth mAuth;
    private RegisterUserActivity.UserPhoneVerificationResult mCallback;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private Button resendBtn;
    private EditText digit1, digit2, digit3, digit4, digit5, digit6;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String phoneNumber; // Set this before calling resendOtp()
    Context mContext;
    FragmentActivity verifyPhoneBottomSheetActivity;
    Bundle data;

    public VerifyPhoneBottomSheet(String verificationId, PhoneAuthProvider.ForceResendingToken resendToken, Bundle dataBundle,
                                  RegisterUserActivity.UserPhoneVerificationResult callback) {
        this.mVerificationId = verificationId;
        this.mResendToken = resendToken;
        this.mCallback = callback;
        this.data = dataBundle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        if(mContext == null) {
            Log.e("Digihealthlocker :: VerifyPhoneBottomSheet", "Context is null");
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_phone_verification_sheet, container, false);

        mAuth = FirebaseAuth.getInstance();

        resendBtn = view.findViewById(R.id.resendButton);
        Button updateDetailsBtn = view.findViewById(R.id.btnUpdateDetails);
        timerText = view.findViewById(R.id.timerText);

        digit1 = view.findViewById(R.id.otpDigit1);
        digit2 = view.findViewById(R.id.otpDigit2);
        digit3 = view.findViewById(R.id.otpDigit3);
        digit4 = view.findViewById(R.id.otpDigit4);
        digit5 = view.findViewById(R.id.otpDigit5);
        digit6 = view.findViewById(R.id.otpDigit6);

        verifyPhoneBottomSheetActivity = requireActivity();
        
        // Auto verify on OTP complete
        setupOtpInputs();

        startResendCooldown();

        resendBtn.setOnClickListener(v -> resendOtp());

        updateDetailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RegisterUserActivity.class);
            intent.putExtras(data);
            startActivity(intent);
        });

        return view;
    }

    private void startResendCooldown() {
        resendBtn.setEnabled(false);
        resendBtn.setTextColor(Color.parseColor("#808080"));
        timerText.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.format(Locale.getDefault(), "Resend in %ds", millisUntilFinished / 1000));
            }

            public void onFinish() {
                timerText.setText("");
                resendBtn.setTextColor(Color.parseColor("#518085"));
                resendBtn.setEnabled(true);
            }
        }.start();
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {digit1, digit2, digit3, digit4, digit5, digit6};

        for (int i = 0; i < otpFields.length; i++) {
            int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        otpFields[currentIndex - 1].requestFocus();
                    }

                    // Check if all 6 digits are filled
                    StringBuilder otpBuilder = new StringBuilder();
                    for (EditText field : otpFields) {
                        otpBuilder.append(field.getText().toString());
                    }
                    String otp = otpBuilder.toString();

                    if (otp.length() == 6) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                        verifyOtpAndSignIn(credential);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void resendOtp() {
        if (mResendToken == null) {
            Util.makeToast(getContext(), "Cannot resend yet. Try again later.", 1);
            return;
        }

        final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d("FirebaseSuccess", "onCodeSent: " + verificationId);
                        mVerificationId = verificationId;
                        mResendToken = token;
                        Util.makeToast(mContext, "OTP resent successfully", 1);
                        startResendCooldown();
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d("FirebaseSuccess", "onVerificationCompleted: " + credential);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.w("FirebaseError", "onVerificationFailed", e);
                        String errorMessage = e instanceof FirebaseAuthInvalidCredentialsException
                                ? "Invalid phone number format."
                                : e.getMessage();
                        Util.makeToast(mContext, "Error: " + errorMessage, 1);
                    }
                };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(verifyPhoneBottomSheetActivity)
                .setCallbacks(callbacks)
                .setForceResendingToken(mResendToken)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtpAndSignIn(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mCallback.onPhoneVerificationResult(true);
                        dismiss();
                    } else {
                        Util.makeToast(getContext(), "Invalid OTP. Please try again.", 1);
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(verifyPhoneBottomSheetActivity, task -> {
                    if (task.isSuccessful()) {
                        mCallback.onPhoneVerificationResult(true);
                        Log.d("FirebaseSuccess", "User signed in successfully.");

                    } else {
                        Exception exception = task.getException();
                        Log.w("FirebaseError", "signInWithCredential:failure", exception);
                        String errorMessage = "Authentication failed.";
                        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "The verification code is invalid.";
                        }
                        Util.makeToast(mContext, "Error: " + errorMessage, 1);
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
