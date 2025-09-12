package com.example.a3rd.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotFragment extends Fragment {

    private EditText etEmail;
    private Button btnSend;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.forget, container, false);

        etEmail = root.findViewById(R.id.forgetEmail);
        btnSend = root.findViewById(R.id.btnSendReset);

        mAuth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email required");
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return root;
    }
}
