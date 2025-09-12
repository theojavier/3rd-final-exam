package com.example.a3rd.ui.personal_info;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.a3rd.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView labelName, labelStudentNo, labelGender, labelDob, labelCivil,
            labelNationality, labelProgram, labelYearBlock, labelSemester;

    private FirebaseAuth auth;
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

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadProfile();

        return view;
    }

    private void loadProfile() {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();

                        labelName.setText(doc.getString("name"));
                        labelStudentNo.setText(doc.getString("studentNo"));
                        labelGender.setText(doc.getString("gender"));
                        labelDob.setText(doc.getString("dob"));
                        labelCivil.setText(doc.getString("civilStatus"));
                        labelNationality.setText(doc.getString("nationality"));
                        labelProgram.setText(doc.getString("program"));
                        labelYearBlock.setText(doc.getString("yearBlock"));
                        labelSemester.setText(doc.getString("semester"));

                        // Load profile image if URL exists
                        String imageUrl = doc.getString("profileImage");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person)
                                    .into(profileImage);
                        }
                    }
                });
    }
}