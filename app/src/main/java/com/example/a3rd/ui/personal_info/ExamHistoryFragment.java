package com.example.a3rd.ui.personal_info;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment; // ✅ Use Fragment, not AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.adapters.ExamHistoryAdapter;
import com.example.a3rd.models.ExamHistory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
        loadExamHistory();

        return view;
    }

    private void loadExamHistory() {
        firestore.collection("examHistory")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            historyList.clear();
                            for (DocumentSnapshot doc : task.getResult()) {
                                String subject = doc.getString("subject");
                                String date = doc.getString("date");
                                String score = doc.getString("score");

                                historyList.add(new ExamHistory(subject, date, score));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Error loading exam history",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("ExamHistory", "Error getting data", task.getException());
                        }
                    }
                });
    }
}