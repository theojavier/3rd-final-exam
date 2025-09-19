package com.example.a3rd.ui.personal_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.a3rd.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView labelName, labelStudentNo, labelGender, labelDob, labelCivil,
            labelNationality, labelProgram, labelYearBlock, labelSemester;

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Views
        profileImage = view.findViewById(R.id.profileImage);
        labelName = view.findViewById(R.id.label_name);
        labelStudentNo = view.findViewById(R.id.label_student_no);
        labelGender = view.findViewById(R.id.label_gender);
        labelDob = view.findViewById(R.id.label_dob);
        labelCivil = view.findViewById(R.id.label_civil);
        labelNationality = view.findViewById(R.id.label_nationality);
        labelProgram = view.findViewById(R.id.label_program);
        labelYearBlock = view.findViewById(R.id.label_year_block);
        labelSemester = view.findViewById(R.id.label_semester);

        firestore = FirebaseFirestore.getInstance();

        loadProfile();

        return view;
    }

    private void loadProfile() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        labelName.setText("Name: " + doc.getString("name"));
                        labelStudentNo.setText("Student No.: " + doc.getString("studentId"));
                        labelGender.setText("Gender: " + doc.getString("gender"));

                        // dob vs ddb check
                        String dob = doc.getString("dob");
                        labelDob.setText("Date of Birth: " + (dob != null ? dob : "N/A"));

                        labelCivil.setText("Civil Status: " + doc.getString("civilStatus"));
                        labelNationality.setText("Nationality: " + doc.getString("nationality"));
                        labelProgram.setText("Program: " + doc.getString("program"));

                        // 🔹 FIX: Firestore field is "tearBlock"
                        String yearBlock = doc.getString("yearBlock");
                        labelYearBlock.setText("Year/Block: " + (yearBlock != null ? yearBlock : "N/A"));

                        labelSemester.setText("Semester: " + doc.getString("semester"));

                        // 🔹 FIX: Ensure Imgur direct link
                        String imageUrl = doc.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            if (imageUrl.contains("imgur.com") && !imageUrl.contains("i.imgur.com")) {
                                // convert to direct link if user pasted page link
                                imageUrl = imageUrl.replace("imgur.com", "i.imgur.com") + ".jpg";
                            }

                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person) // fallback if broken link
                                    .into(profileImage);
                        }
                    } else {
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }
}