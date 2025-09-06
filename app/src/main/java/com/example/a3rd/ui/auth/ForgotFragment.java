package com.example.a3rd.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;

public class ForgotFragment extends Fragment {

    private EditText emailField;
    private Button sendButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.forget, container, false);

        emailField = root.findViewById(R.id.forgetEmail);
        sendButton = root.findViewById(R.id.btnSendReset);

        sendButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                // TODO: Replace this with server request to send reset link
            }
        });

        return root;
    }
}
