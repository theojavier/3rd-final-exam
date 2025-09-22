package com.example.a3rd.ui.exam;

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
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Date;
import com.example.a3rd.R;
import com.example.a3rd.adapters.ExamAdapter;
import com.example.a3rd.models.ExamModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Exam_item_Fragment extends Fragment {

    private RecyclerView recyclerExams;
    private ExamAdapter adapter;
    private List<ExamModel> examList;
    private FirebaseFirestore db;

    public Exam_item_Fragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam_layout, container, false);

        recyclerExams = view.findViewById(R.id.recycler_exams);
        recyclerExams.setLayoutManager(new LinearLayoutManager(getContext()));

        examList = new ArrayList<>();
        adapter = new ExamAdapter(getContext(), examList);
        recyclerExams.setAdapter(adapter);

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

        // 1️⃣ Get start & end of current week
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Set to Monday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = calendar.getTime();

        // Set to Sunday
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEnd = calendar.getTime();

        Log.d("Exam_item_Fragment", "Week range: " + weekStart + " → " + weekEnd);

        // 2️⃣ Get student’s program and yearBlock
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

                        // 3️⃣ Load exams only for this program + yearBlock + current week
                        db.collection("exams")
                                .whereEqualTo("program", studentProgram)
                                .whereEqualTo("yearBlock", studentYearBlock)
                                .whereGreaterThanOrEqualTo("startTime", new Timestamp(weekStart))
                                .whereLessThanOrEqualTo("endTime", new Timestamp(weekEnd))
                                .addSnapshotListener((querySnapshot, e) -> {
                                    if (e != null) {
                                        Log.e("Exam_item_Fragment", "Listen failed", e);
                                        return;
                                    }

                                    examList.clear();
                                    if (querySnapshot != null) {
                                        for (DocumentSnapshot doc : querySnapshot) {
                                            ExamModel exam = doc.toObject(ExamModel.class);
                                            if (exam != null) {
                                                exam.setId(doc.getId());
                                                examList.add(exam);
                                            }
                                        }
                                    }

                                    adapter.notifyDataSetChanged();
                                });

                    } else {
                        Log.w("Exam_item_Fragment", "No matching user found for studentId: " + currentStudentId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Exam_item_Fragment", "Error loading user", e)
                );
    }
}