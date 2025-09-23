package com.example.a3rd.ui.auth;

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

import com.example.a3rd.MainActivity;
import com.example.a3rd.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private EditText etStudentId, etPassword;
    private Button btnLogin;
    private TextView tvForgot;

    private FirebaseFirestore db;

    public LoginFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

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
                                if ("student".equalsIgnoreCase(role)) {
                                    // ✅ Save new session
                                    SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
                                    String userDocId = document.getId(); // Firestore doc ID
                                    prefs.edit()
                                            .clear() // wipe old session
                                            .putString("userId", userDocId)   // Firestore document ID
                                            .putString("studentId", studentId) // Student’s own ID
                                            .putBoolean("isLoggedIn", true)    // logged in flag
                                            .apply();
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).loadUserProfile();
                                    }

                                    Toast.makeText(getActivity(), "Welcome Student!", Toast.LENGTH_SHORT).show();

                                    // Ensure notifications exist and start the listener (use userDocId)
                                    if (getActivity() instanceof MainActivity) {
                                        MainActivity main = (MainActivity) requireActivity();
                                        main.ensureUserNotificationsCollectionMinimal(userDocId, () -> {
                                            // This will run after the "welcome" notification is created
                                            main.startNotificationListener(userDocId);
                                            Log.d(TAG, "Created minimal notification subcollection");
                                        });

                                        // Create notification docs for matched exams, then start listener.
                                        main.ensureUserNotificationsCollection(userDocId, () -> {
                                            // Start the listener after ensure finishes (safe to call even if already started)
                                            main.startNotificationListener(userDocId);

                                        });

                                    }

                                    // Navigate to home
                                    Navigation.findNavController(v).navigate(R.id.nav_home);
                                } else {
                                    Toast.makeText(getActivity(), "Access denied (not a student)", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Invalid Student ID or Password", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast.makeText(getActivity(), "Student ID not found", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Firestore error", e);
                    });
        });

        // Forgot password
        tvForgot.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_forgot));

        return root;
    }
}
