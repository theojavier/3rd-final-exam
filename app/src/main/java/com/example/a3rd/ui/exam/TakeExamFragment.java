package com.example.a3rd.ui.exam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TakeExamFragment extends Fragment {

    private TextView tvSubject, tvStartTime, tvTeacher, tvExamDuration;
    private Button btnStartExam;

    private FirebaseFirestore db;
    private String studentId;
    private String examId;
    private long startMillis = -1;
    private long endMillis = -1;

    public TakeExamFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.take_exam, container, false);

        tvSubject = view.findViewById(R.id.tv_subject);
        tvStartTime = view.findViewById(R.id.tv_start_time);
        tvTeacher = view.findViewById(R.id.tv_teacher);
        tvExamDuration = view.findViewById(R.id.tv_exam_duration);
        btnStartExam = view.findViewById(R.id.btn_start_exam);

        db = FirebaseFirestore.getInstance();

        // ✅ Load studentId from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        studentId = prefs.getString("studentId", null);

        if (studentId == null) {
            btnStartExam.setEnabled(false);
            btnStartExam.setText("Not logged in");
            return view;
        }

        // ✅ Get examId and schedule from arguments
        Bundle args = getArguments();
        if (args == null) {
            Toast.makeText(getContext(), "No arguments passed", Toast.LENGTH_SHORT).show();
            return view;
        }

        examId = args.getString("examId");
        startMillis = args.getLong("startTime", -1);
        endMillis = args.getLong("endTime", -1);

        if (examId == null) {
            Toast.makeText(getContext(), "No exam selected", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 🔹 Fetch exam details
        db.collection("exams").document(examId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Exam not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String subject = doc.getString("subject");
                    String teacherId = doc.getString("teacherId");
                    Timestamp startTimestamp = doc.getTimestamp("startTime");
                    Timestamp endTimestamp = doc.getTimestamp("endTime");

                    if (startMillis == -1 && startTimestamp != null) {
                        startMillis = startTimestamp.toDate().getTime();
                    }
                    if (endMillis == -1 && endTimestamp != null) {
                        endMillis = endTimestamp.toDate().getTime();
                    }

                    if (subject != null) tvSubject.setText(subject);
                    if (startMillis > 0) {
                        Date startDate = new Date(startMillis);
                        tvStartTime.setText("Start: " + DateFormat.format("MMM d, yyyy h:mm a", startDate));
                    }
                    if (startMillis > 0 && endMillis > 0) {
                        tvExamDuration.setText(
                                DateFormat.format("MMM d, yyyy h:mm a", new Date(startMillis)) +
                                        " - " + DateFormat.format("h:mm a", new Date(endMillis))
                        );
                    }

                    if (teacherId != null) {
                        db.collection("users").document(teacherId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String teacherName = userDoc.getString("name");
                                    tvTeacher.setText("Teacher: " + (teacherName != null ? teacherName : "Unknown"));
                                })
                                .addOnFailureListener(e -> tvTeacher.setText("Teacher: Unknown"));
                    }

                    // ✅ Compute exam status from current time
                    long now = System.currentTimeMillis();
                    String examStatus;
                    if (now < startMillis) {
                        examStatus = "upcoming";
                    } else if (now > endMillis) {
                        examStatus = "ended";
                    } else {
                        examStatus = "ongoing";
                    }

                    // ✅ Correct Firestore path for results
                    DocumentReference resultRef = db.collection("examResults")
                            .document(examId)
                            .collection(studentId)
                            .document("result");

                    resultRef.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String studentStatus = snapshot.getString("status");

                            if ("completed".equals(studentStatus)) {
                                // Exam already finished → only allow viewing result
                                btnStartExam.setText("View Result");
                                btnStartExam.setOnClickListener(v -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("score", snapshot.getLong("score").intValue());
                                    bundle.putInt("total", snapshot.getLong("total").intValue());
                                    bundle.putString("examId", examId);
                                    bundle.putString("studentId", studentId);
                                    Navigation.findNavController(v).navigate(R.id.examResultFragment, bundle);
                                });

                            } else if ("in-progress".equals(studentStatus)) {
                                // Exam started but not submitted → allow resume
                                btnStartExam.setEnabled(true);
                                btnStartExam.setText("Resume Exam");
                                btnStartExam.setOnClickListener(v -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("examId", examId);
                                    bundle.putString("studentId", studentId);
                                    Navigation.findNavController(v).navigate(R.id.examFragment, bundle);
                                });
                            }

                        } else {
                            // No record yet → use exam status
                            switch (examStatus) {
                                case "upcoming":
                                    btnStartExam.setEnabled(false);
                                    btnStartExam.setText("Exam not started yet");
                                    break;

                                case "ended":
                                    btnStartExam.setEnabled(false);
                                    btnStartExam.setText("Exam ended");
                                    break;

                                case "ongoing":
                                    btnStartExam.setEnabled(true);
                                    btnStartExam.setText("Start Exam");
                                    btnStartExam.setOnClickListener(v -> {
                                        Toast.makeText(getContext(), "Exam Started!", Toast.LENGTH_SHORT).show();

                                        Map<String, Object> resultData = new HashMap<>();
                                        resultData.put("examId", examId);
                                        resultData.put("studentId", studentId);
                                        resultData.put("status", "in-progress");
                                        resultData.put("timestamp", now);

                                        resultRef.set(resultData);

                                        Bundle bundle = new Bundle();
                                        bundle.putString("examId", examId);
                                        bundle.putString("studentId", studentId);
                                        Navigation.findNavController(v).navigate(R.id.examFragment, bundle);
                                    });
                                    break;
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load exam", Toast.LENGTH_SHORT).show());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_nav_home_to_nav_exam_item_page);
                    }
                }
        );
    }
}
