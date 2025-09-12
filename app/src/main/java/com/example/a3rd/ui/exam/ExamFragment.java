package com.example.a3rd.ui.exam;

import android.os.Bundle;
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
import com.example.a3rd.models.ExamModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExamFragment extends Fragment {

    private RecyclerView recyclerExams;
    private ExamAdapter adapter;
    private List<ExamModel> examList;
    private FirebaseFirestore db;

    public ExamFragment() {
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
        db.collection("exams").get().addOnSuccessListener(query -> {
            examList.clear();
            for (DocumentSnapshot doc : query) {
                ExamModel exam = doc.toObject(ExamModel.class);
                if (exam != null) {
                    examList.add(exam);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}