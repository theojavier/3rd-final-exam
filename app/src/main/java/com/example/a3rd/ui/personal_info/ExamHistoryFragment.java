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
import com.example.a3rd.adapters.ExamAdapter;
import com.example.a3rd.adapters.ExamHistoryAdapter;
import com.example.a3rd.models.ExamModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExamHistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private ExamAdapter adapter;
    private List<ExamModel> examList;
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
        adapter = new ExamAdapter(getContext(), examList);
        recyclerHistory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadExams();

        return view;
    }

    private void loadExams() {
        // 👇 Get logged-in studentId
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentStudentId = prefs.getString("studentId", null);

        if (currentStudentId == null) {
            Log.e("Exam_item_Fragment", "No studentId found in SharedPreferences");
            return;
        }

        // 1️⃣ Get student’s program and yearBlock
        db.collection("users")
                .whereEqualTo("studentId", currentStudentId)
                .limit(1)
                .get()
                .addOnSuccessListener(userQuery -> {
                    if (!userQuery.isEmpty()) {
                        DocumentSnapshot userDoc = userQuery.getDocuments().get(0);
                        String studentProgram = userDoc.getString("program");
                        String studentYearBlock = userDoc.getString("yearBlock");

                        Log.d("Exam_item_Fragment", "Student program: " + studentProgram + ", yearBlock: " + studentYearBlock);

                        // 2️⃣ Load exams only for this program + yearBlock
                        db.collection("exams")
                                .whereEqualTo("program", studentProgram)
                                .whereEqualTo("yearBlock", studentYearBlock)
                                .get()
                                .addOnSuccessListener(query -> {
                                    examList.clear();
                                    for (DocumentSnapshot doc : query) {
                                        Log.d("Exam_item_Fragment", "Exam found: " + doc.getData());
                                        ExamModel exam = doc.toObject(ExamModel.class);
                                        if (exam != null) {
                                            exam.setId(doc.getId()); // ✅ store Firestore document ID
                                            examList.add(exam);
                                        }
                                    }

                                    if (examList.isEmpty()) {
                                        Log.w("Exam_item_Fragment", "No exams found for program/yearBlock");
                                    }

                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Log.e("Exam_item_Fragment", "Error loading exams", e)
                                );
                    } else {
                        Log.w("Exam_item_Fragment", "No matching user found for studentId: " + currentStudentId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Exam_item_Fragment", "Error loading user", e)
                );
    }
}


