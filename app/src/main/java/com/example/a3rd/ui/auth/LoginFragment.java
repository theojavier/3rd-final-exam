package com.example.a3rd.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgot, tvRegister;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Views
        etEmail = root.findViewById(R.id.Email);
        etPassword = root.findViewById(R.id.Password);
        btnLogin = root.findViewById(R.id.btnLogin);
        tvForgot = root.findViewById(R.id.forgotPassword);

        // Login button
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                            // Navigate to HomeFragment
                            Navigation.findNavController(v).navigate(R.id.nav_home);
                        } else {
                            Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("LoginFragment", "Login failed", task.getException());
                        }
                    });
        });

        // Forgot password → navigate to ForgotFragment
        tvForgot.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_forgot));

        // Optional: Register → navigate to RegisterFragment if exists

        return root;
    }
}