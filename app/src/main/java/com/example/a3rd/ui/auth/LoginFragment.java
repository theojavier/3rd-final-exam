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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgot;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkIfStudent(user.getUid(), v);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Login failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("LoginFragment", "Login failed", task.getException());
                        }
                    });
        });

        // Forgot password → navigate to ForgotFragment
        tvForgot.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_forgot));

        return root;
    }

    private void checkIfStudent(String uid, View v) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        if ("student".equalsIgnoreCase(role)) {
                            Toast.makeText(getActivity(), "Welcome Student!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).navigate(R.id.nav_home);
                        } else {
                            Toast.makeText(getActivity(),
                                    "Only students can log in here", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(getActivity(),
                                "User record not found in Firestore", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(),
                            "Failed to fetch user data", Toast.LENGTH_LONG).show();
                    Log.e("LoginFragment", "Firestore error", e);
                    mAuth.signOut();
                });
    }
}