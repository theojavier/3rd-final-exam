package com.example.a3rd.ui.exam;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class ExamHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExamHistoryAdapter adapter;
    private List<ExamHistory> historyList;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exam_history);

        recyclerView = findViewById(R.id.recyclerExamHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new ExamHistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadExamHistory();
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
                            Toast.makeText(ExamHistoryActivity.this,
                                    "Error loading exam history",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("ExamHistory", "Error getting data", task.getException());
                        }
                    }
                });
    }
}