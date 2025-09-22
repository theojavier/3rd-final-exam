package com.example.a3rd.ui.personal_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.adapters.ExamAdapterhistory;
import com.example.a3rd.models.ExamHistoryModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExamHistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private ExamAdapterhistory adapter;
    private List<ExamHistoryModel> examList;
    private FirebaseFirestore db;


    public ExamHistoryFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam_history, container, false);

        recyclerHistory = view.findViewById(R.id.recyclerExamHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        examList = new ArrayList<>();
        adapter = new ExamAdapterhistory(getContext(), examList);
        recyclerHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadExams();

        return view;
    }

    private void loadExams() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentStudentId = prefs.getString("studentId", null);

        if (currentStudentId == null) {
            Log.e("ExamHistoryFragment", "❌ No studentId found in SharedPreferences");
            return;
        }

        Log.d("ExamHistoryFragment", "✅ Using studentId: " + currentStudentId);

        db.collection("examResults")
                .get()
                .addOnSuccessListener(examIds -> {
                    Log.d("ExamHistoryFragment", "✅ Found " + examIds.size() + " exam(s) in examResults");
                    examList.clear();

                    for (DocumentSnapshot examDoc : examIds) {
                        String examId = examDoc.getId();
                        Log.d("ExamHistoryFragment", "➡ Checking examId: " + examId);

                        db.collection("examResults")
                                .document(examId)
                                .collection(currentStudentId)   // 👈 studentId is a subcollection
                                .document("result")
                                .get()
                                .addOnSuccessListener(studentDoc -> {
                                    if (studentDoc.exists()) {
                                        Log.d("ExamHistoryFragment", "✅ Result FOUND for examId: " + examId + " studentId: " + currentStudentId);
                                        Log.d("ExamHistoryFragment", "🔥 Raw data: " + studentDoc.getData());

                                        ExamHistoryModel exam = studentDoc.toObject(ExamHistoryModel.class);
                                        if (exam != null) {
                                            exam.setId(examId); // store examId
                                            Log.d("ExamHistoryFragment", "📌 Parsed Exam -> Id: " + exam.getId()
                                                    + ", Status: " + exam.getStatus()
                                                    + ", SubmittedAt: " + exam.getSubmittedAt());

                                            examList.add(exam);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            Log.e("ExamHistoryFragment", "❌ Could not parse ExamHistoryModel for examId: " + examId);
                                        }
                                    } else {
                                        Log.w("ExamHistoryFragment", "⚠ No result found for examId: " + examId + " and studentId: " + currentStudentId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ExamHistoryFragment", "❌ Error fetching student exam for examId: " + examId, e);
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ExamHistoryFragment", "❌ Error loading examResults", e)
                );
    }
}


