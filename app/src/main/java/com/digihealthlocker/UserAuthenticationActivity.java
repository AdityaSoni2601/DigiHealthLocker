package com.digihealthlocker;

import android.content.Intent;
import androidx.credentials.CredentialManager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.digihealthlocker.userregistration.RegisterUserActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class UserAuthenticationActivity extends AppCompatActivity {

    EditText userName, userPassword;
    Button signInBtn;
    FirebaseAuth mAuth;
    CredentialManager credentialManager;

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

        credentialManager = CredentialManager.create(this);

//        userName = findViewById(R.id.userEmailOrPhoneNumber);
//        userPassword = findViewById(R.id.userPassword);
//        signInBtn = findViewById(R.id.signInBtn);

        mAuth = FirebaseAuth.getInstance();

//        signInBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String username = userName.getText().toString();
//                String password = userPassword.getText().toString();
//                if (Util.isNullOrEmpty(username) || Util.isNullOrEmpty(password)) {
//                    Util.makeToast(UserAuthenticationActivity.this, "Please enter both username and password", Toast.LENGTH_LONG);
//                }
//                boolean isPhoneNumberValid = Util.isValidPhoneNumber(username);
//                boolean isEmailValid = Util.isValidEmailAddress(username);
//                if(!isPhoneNumberValid || !isEmailValid) {
//                    Util.makeToast(UserAuthenticationActivity.this, "Please enter a valid email or phone number", Toast.LENGTH_LONG);
//                }
//
//                if (isPhoneNumberValid) {
////                    validatePhoneNumberAndPassword(username, password);
//                } else {
//                    validateEmailAndPassword(username, password);
//                }
//            }
//        });

//        MaterialButton passkeyButton = findViewById(R.id.signInWithPasskeyBtn);
//        passkeyButton.setOnClickListener(v -> signInWithPasskey());

        MaterialButton signUpButton = findViewById(R.id.signUpBtn);
        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterUserActivity.class));
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

//    private void signInWithPasskey() {
//        List<CredentialOption> options = new ArrayList<>();
//        options.add(new GetPublicKeyCredentialOption.Builder()
//                .setRequestJson(getPasskeyRequestJson()) // From your backend
//                .build());
//
//        GetCredentialRequest request = new GetCredentialRequest.Builder()
//                .addCredentialOptions(options)
//                .build();
//
//        credentialManager.getCredentialAsync(
//                this,
//                request,
//                null, // cancellationSignal
//                getMainExecutor(),
//                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
//                    @Override
//                    public void onResult(GetCredentialResponse response) {
//                        Credential credential = response.getCredential();
//                        if (credential instanceof PublicKeyCredential) {
//                            String json = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
//                            // 🔐 Send this to your backend for validation
//                            Toast.makeText(UserAuthenticationActivity.this, "Passkey login success!", Toast.LENGTH_SHORT).show();
//                            Log.d("TAG", "Passkey credential: " + json);
//                        }
//                    }
//
//                    @Override
//                    public void onError(GetCredentialException e) {
//                        Log.e("TAG", "Credential error: " + e.getMessage(), e);
//                        Toast.makeText(UserAuthenticationActivity.this, "Login failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//    }
//
//    // This should come from your backend
//    private String getPasskeyRequestJson() {
//        // Example request - you must replace this with actual response from your server
//        return "{"
//                + "\"challenge\":\"random-challenge-from-server\","
//                + "\"rpId\":\"your-domain.com\","
//                + "\"allowCredentials\":[],"
//                + "\"userVerification\":\"preferred\""
//                + "}";
//    }
}