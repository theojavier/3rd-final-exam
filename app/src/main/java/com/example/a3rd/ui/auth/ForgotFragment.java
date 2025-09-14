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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotFragment extends Fragment {

    private EditText etStudentId, etEmail;
    private Button btnVerify;

    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.forget, container, false);

        // Inputs
        etStudentId = root.findViewById(R.id.forgetStudentId);
        etEmail = root.findViewById(R.id.forgetEmail);
        btnVerify = root.findViewById(R.id.btnVerify);

        db = FirebaseFirestore.getInstance();

        btnVerify.setOnClickListener(v -> {
            String studentId = etStudentId.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(studentId)) {
                etStudentId.setError("Student ID required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email required");
                return;
            }

            // 🔍 Check Firestore for both StudentID + Email
            db.collection("users")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            String userId = document.getId();

                            // ✅ Pass userId to ResetPasswordFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("userId", userId);

                            Toast.makeText(getActivity(), "Verified! Now set your new password.", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(v).navigate(R.id.nav_reset_password, bundle);
                        } else {
                            Toast.makeText(getActivity(), "No account found for this Student ID and Email", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("ForgotFragment", "Firestore error", e);
                    });
        });

        return root;
    }
}