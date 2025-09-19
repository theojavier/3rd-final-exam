package com.example.a3rd.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private EditText etStudentId, etPassword;
    private Button btnLogin;
    private TextView tvForgot;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

        // Firestore only (no FirebaseAuth since we’re using studentId + password)
        db = FirebaseFirestore.getInstance();

        // Views
        etStudentId = root.findViewById(R.id.student_id);
        etPassword = root.findViewById(R.id.Password);
        btnLogin = root.findViewById(R.id.btnLogin);
        tvForgot = root.findViewById(R.id.forgotPassword);

        // Login button
        btnLogin.setOnClickListener(v -> {
            String studentId = etStudentId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(studentId)) {
                etStudentId.setError("Student ID required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password required");
                return;
            }

            // Step 1: Find user by studentId
            db.collection("users")
                    .whereEqualTo("studentId", studentId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            String storedPassword = document.getString("password");
                            String role = document.getString("role");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                // ✅ Correct StudentID + Password
                                if ("student".equalsIgnoreCase(role)) {

                                    // Save Firestore userId in SharedPreferences
                                    SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                    prefs.edit().putString("userId", document.getId()).apply();
                                    getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                                            .edit()
                                            .putString("userId", document.getId())   // Firestore document ID
                                            .putString("studentId", studentId)       // Student’s own ID
                                            .apply();


                                    Toast.makeText(getActivity(), "Welcome Student!", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(v).navigate(R.id.nav_home);
                                } else {
                                    Toast.makeText(getActivity(), "Access denied (not a student)", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                // ❌ Wrong password
                                Toast.makeText(getActivity(), "Invalid Student ID or Password", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), "Student ID not found", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
                        Log.e("LoginFragment", "Firestore error", e);
                    });
        });

        // Forgot password → navigate to ForgotFragment
        tvForgot.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_forgot));

        return root;
    }
}