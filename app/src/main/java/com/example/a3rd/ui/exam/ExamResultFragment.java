package com.example.a3rd.ui.exam;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ExamResultFragment extends Fragment {

    private static final String TAG = "ExamResultFragment";

    private TextView tvScoreSummary;
    private LinearLayout layoutAnswers;

    private String examId;
    private String studentId;

    private FirebaseFirestore db;

    public ExamResultFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_result, container, false);

        tvScoreSummary = view.findViewById(R.id.tv_score_summary);
        layoutAnswers = view.findViewById(R.id.layout_answers);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            examId = getArguments().getString("examId");
            studentId = getArguments().getString("studentId");
        }

        if (examId == null || studentId == null) {
            Toast.makeText(requireContext(), "Missing examId or studentId", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 🔹 Load meta info first, then answers
        loadExamResult();
        loadAnswers();

        return view;
    }

    /** Loads the exam result meta (score, total, status) */
    private void loadExamResult() {
        db.collection("examResults")
                .document(examId)
                .collection(studentId)
                .document("result")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long score = doc.getLong("score");
                        Long total = doc.getLong("total");

                        if (score != null && total != null) {
                            tvScoreSummary.setText("Score: " + score + "/" + total);
                        } else {
                            tvScoreSummary.setText("Result not found");
                        }
                    } else {
                        tvScoreSummary.setText("Result not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load exam result", e);
                    Toast.makeText(requireContext(), "Failed to load result", Toast.LENGTH_SHORT).show();
                });
    }

    /** Loads all answers from the answers subcollection */
    private void loadAnswers() {
        db.collection("examResults")
                .document(examId)
                .collection(studentId)
                .document("result")
                .collection("answers")
                .get()
                .addOnSuccessListener(query -> {
                    layoutAnswers.removeAllViews();
                    for (QueryDocumentSnapshot doc : query) {
                        String question = doc.getString("question");
                        String answer = doc.getString("answer");

                        TextView tv = new TextView(getContext());
                        tv.setText("Q: " + question + "\nYour Answer: " + answer);
                        tv.setPadding(0, 8, 0, 8);
                        layoutAnswers.addView(tv);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load answers", e);
                    Toast.makeText(requireContext(), "Failed to load answers", Toast.LENGTH_SHORT).show();
                });
    }
}
