package com.example.a3rd.ui.exam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;

import java.util.HashMap;
import java.util.Map;

public class ExamResultFragment extends Fragment {

    private TextView tvScoreSummary;
    private LinearLayout layoutAnswers;

    public ExamResultFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_result, container, false);

        tvScoreSummary = view.findViewById(R.id.tv_score_summary);
        layoutAnswers = view.findViewById(R.id.layout_answers);

        if (getArguments() != null) {
            int score = getArguments().getInt("score", 0);
            int total = getArguments().getInt("total", 0);
            tvScoreSummary.setText("Score: " + score + "/" + total);

            Map<String, Object> answers = (HashMap<String, Object>) getArguments().getSerializable("answers");
            if (answers != null) {
                for (Map.Entry<String, Object> entry : answers.entrySet()) {
                    TextView tv = new TextView(getContext());
                    tv.setText("Q: " + entry.getKey() + "\nYour Answer: " + entry.getValue());
                    tv.setPadding(0, 8, 0, 8);
                    layoutAnswers.addView(tv);
                }
            }
        }

        return view;
    }
}