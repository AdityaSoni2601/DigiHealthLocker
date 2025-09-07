//// VerifyEmailBottomSheet.java
//package com.digihealthlocker;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.credentials.CreatePublicKeyCredentialRequest;
//import androidx.credentials.CredentialManager;
//
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
//
//public class AskPassKeyCreationBottomSheet extends BottomSheetDialogFragment {
//    Context mContext;
//    public AskPassKeyCreationBottomSheet() {}
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mContext = getContext();
//        if(mContext == null) {
//            Log.e("Digihealthlocker :: VerifyEmailBottomSheet", "Context is null");
//            return null;
//        }
//        View view = inflater.inflate(R.layout.ask_passkey_creation_bottom_sheet, container, false);
//
//        Button continueBtn = view.findViewById(R.id.continueButton);
//        Button notNowButton = view.findViewById(R.id.notNowButton);
//
//        continueBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                createPasskey();
//            }
//        });
//
//        notNowButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(mContext, DashboardActivity.class));
//            }
//        });
//        return view;
//    }
//
//    private void createPasskey() {
//        Context context = getContext();
//        if (context == null) return;
//
//        // Get CredentialManager instance
//        CredentialManager credentialManager = CredentialManager.create(mContext);
//
//        // Normally, you fetch this JSON from your backend server (WebAuthn challenge etc.)
//        String rpId = "example.com"; // Replace with your server's domain
//        String challenge = "random_challenge_from_server";
//
//        // Build the Passkey creation request
//        CreatePublicKeyCredentialRequest createRequest =
//                new CreatePublicKeyCredentialRequest(
//                        "{\n" +
//                                "  \"challenge\": \"" + challenge + "\",\n" +
//                                "  \"rp\": {\"id\": \"" + rpId + "\", \"name\": \"DigiHealthLocker\"},\n" +
//                                "  \"user\": {\"id\": \"user123\", \"name\": \"test@example.com\", \"displayName\": \"Test User\"},\n" +
//                                "  \"pubKeyCredParams\": [{\"type\": \"public-key\", \"alg\": -7}]\n" +
//                                "}"
//                );
//
//        credentialManager.createCredentialAsync(
//                createRequest,
//                requireActivity().getMainExecutor(),
//                new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
//                    @Override
//                    public void onResult(CreateCredentialResponse result) {
//                        // ✅ Success - passkey created
//                        Log.d("Passkey", "Passkey created: " + result.getClass().getSimpleName());
//                        Toast.makeText(context, "Passkey created successfully!", Toast.LENGTH_SHORT).show();
//
//                        // Navigate user to dashboard
//                        startActivity(new Intent(context, DashboardActivity.class));
//                    }
//
//                    @Override
//                    public void onError(CreateCredentialException e) {
//                        // ❌ Handle failure
//                        Log.e("Passkey", "Error creating passkey", e);
//                        Toast.makeText(context, "Failed to create passkey: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//    }
//
//
//}
