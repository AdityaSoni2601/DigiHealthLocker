// VerifyEmailBottomSheet.java
package com.digihealthlocker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Locale;

import Utility.AppHandler;
import Utility.SharedPref;
import Utility.Util;

public class VerifyEmailBottomSheet extends BottomSheetDialogFragment {
    private FirebaseUser currentUser;
    FirebaseAuth mAuth = null;
    private AppHandler verificationCheckHandler;
    private Runnable verificationCheckRunnable;
    private RegisterUserActivity.UserEmailVerificationResult callback;
    private TextView timerText;
    private CountDownTimer countDownTimer;
    Button resendBtn;
    Bundle data;
    Context mContext;
    public VerifyEmailBottomSheet(Bundle dataBundle, RegisterUserActivity.UserEmailVerificationResult callback) {
        this.callback = callback;
        this.data = dataBundle;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        if(mContext == null) {
            Log.e("Digihealthlocker :: VerifyEmailBottomSheet", "Context is null");
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_email_verification_sheet, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        resendBtn = view.findViewById(R.id.resendButton);
        Button updateDetailsBtn = view.findViewById(R.id.btnUpdateDetails);
        timerText = view.findViewById(R.id.timerText);

        verificationCheckHandler = AppHandler.getInstance();

        startEmailVerificationPolling();
        startResendCooldown();
        resendBtn.setOnClickListener(v -> {
            if (currentUser != null) {
                currentUser.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Util.makeToast(getContext(), "Verification email sent.", 1);
                                startResendCooldown();
                            } else {
                                Util.makeToast(getContext(), "Failed to resend email.", 1);
                            }
                        });
            }
        });

        updateDetailsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, RegisterUserActivity.class);
            intent.putExtras(data);
            startActivity(intent);
        });

        return view;
    }

    private void startEmailVerificationPolling() {
        verificationCheckRunnable = new Runnable() {
            int tryCount = 0;
            @Override
            public void run() {
                currentUser.reload().addOnCompleteListener(task -> {
                    if (currentUser.isEmailVerified()) {
                        SharedPref.getInstance().setIsEmailVerified(true);
                        Util.makeToast(getContext(), "Email verified! Proceeding...", 1);
                        callback.onEmailVerificationResult(true);
                        dismiss();
                    } else {
                        if(tryCount > 40) {
                            callback.onEmailVerificationResult(false);
                        }
                        tryCount++;
                        verificationCheckHandler.postDelayed(verificationCheckRunnable, 1500);
                    }
                });
            }
        };
        verificationCheckHandler.post(verificationCheckRunnable);
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


    @Override
    public void onDestroy() {
        Log.i("onDestroy", "onDestroy()");
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (verificationCheckHandler != null && verificationCheckRunnable != null) {
            verificationCheckHandler.removeCallbacks(verificationCheckRunnable);
        }
    }
}
