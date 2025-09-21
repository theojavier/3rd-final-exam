package com.example.a3rd.ui.personal_info;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.adapters.ExamHistoryAdapter;
import com.example.a3rd.models.ExamHistory;
import com.example.a3rd.ui.exam.ExamResultFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExamHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExamHistoryAdapter adapter;
    private List<ExamHistory> historyList;

    private FirebaseFirestore firestore;
    private String currentStudentId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.exam_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerExamHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        historyList = new ArrayList<>();
        adapter = new ExamHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        // ✅ Get studentId from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentStudentId = prefs.getString("studentId", null);

        // Load exam history only for this student
        loadExamHistory();

        // ✅ Handle item clicks
        adapter.setOnItemClickListener((history, examId) -> {
            Bundle bundle = new Bundle();
            bundle.putString("examId", examId);

            ExamResultFragment fragment = new ExamResultFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadExamHistory() {
        if (currentStudentId == null) {
            Toast.makeText(requireContext(), "No student logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("examHistory")
                .whereEqualTo("studentId", currentStudentId) // ✅ filter by logged-in student
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        historyList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String examId = doc.getString("examId");
                            String subject = doc.getString("subject");
                            String date = doc.getString("date");
                            String score = doc.getString("score");

                            historyList.add(new ExamHistory(examId, subject, date, score));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(),
                                "Error loading exam history",
                                Toast.LENGTH_SHORT).show();
                        Log.e("ExamHistory", "Error getting data", task.getException());
                    }
                });
    }
}
