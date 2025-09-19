package com.example.a3rd.ui.exam;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a3rd.R;

import java.util.Date;

public class TakeExamFragment extends Fragment {

    private TextView tvSubject, tvStartTime, tvTeacher, tvExamDuration;
    private Button btnStartExam;

    public TakeExamFragment() {
        // Required empty public constructor
    }

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

        // Get arguments from bundle
        Bundle args = getArguments();
        if (args != null) {
            String subject = args.getString("subject");
            String teacherId = args.getString("teacherId");
            long startMillis = args.getLong("startTime", 0);
            long endMillis = args.getLong("endTime", 0);

            Date startDate = new Date(startMillis);
            Date endDate = new Date(endMillis);

            tvSubject.setText(subject);
            tvStartTime.setText("Start: " + DateFormat.format("MMM d, yyyy h:mm a", startDate));
            tvTeacher.setText("Teacher: " + teacherId);
            tvExamDuration.setText(DateFormat.format("MMM d, yyyy h:mm a", startDate) + " - " +
                    DateFormat.format("h:mm a", endDate));

            long now = System.currentTimeMillis();
            if (now < startMillis) {
                btnStartExam.setEnabled(false);
                btnStartExam.setText("Exam not started yet");
            } else if (now > endMillis) {
                btnStartExam.setEnabled(false);
                btnStartExam.setText("Exam ended");
            } else {
                btnStartExam.setEnabled(true);
                btnStartExam.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Exam Started!", Toast.LENGTH_SHORT).show();
                });
            }
        }

        return view;
    }
    
}
