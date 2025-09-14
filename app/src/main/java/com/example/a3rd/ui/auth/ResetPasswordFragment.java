package com.example.a3rd.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPasswordFragment extends Fragment {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnReset;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.reset_password, container, false);

        etNewPassword = root.findViewById(R.id.etNewPassword);
        etConfirmPassword = root.findViewById(R.id.etConfirmPassword);
        btnReset = root.findViewById(R.id.btnResetPassword);

        db = FirebaseFirestore.getInstance();

        // Get userId passed from ForgotFragment
        String userId = getArguments() != null ? getArguments().getString("userId") : null;

        btnReset.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(newPass)) {
                etNewPassword.setError("Enter new password");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            if (userId != null) {
                db.collection("users").document(userId)
                        .update("password", newPass)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getActivity(),
                                    "Password updated successfully!", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(v).navigate(R.id.nav_login);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(),
                                    "Failed to update password", Toast.LENGTH_LONG).show();
                            Log.e("ResetPassword", "Error", e);
                        });
            } else {
                Toast.makeText(getActivity(), "User not found", Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }
}