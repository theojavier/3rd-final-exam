package com.example.a3rd.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.models.ExamHistoryModel;

import java.util.Date;
import java.util.List;

public class ExamAdapterhistory extends RecyclerView.Adapter<ExamAdapterhistory.ExamViewHolder> {

    private Context context;
    private List<ExamHistoryModel> examList;

    public ExamAdapterhistory(Context context, List<ExamHistoryModel> examList) {
        this.context = context;
        this.examList = examList;
    }

    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.exam_history_item, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        ExamHistoryModel exam = examList.get(position);

        Log.d("ExamAdapterhistory", "📌 Binding Exam -> Id: " + exam.getId()
                + ", Status: " + exam.getStatus()
                + ", Score: " + exam.getScore() + "/" + exam.getTotal());

        // Subject
        holder.txtSubject.setText(exam.getSubject() != null ? exam.getSubject() : "Unknown Subject");

        // Submitted date
        if (exam.getSubmittedAt() != null) {
            Date date = exam.getSubmittedAt().toDate();
            String formatted = DateFormat.format("yyyy-MM-dd hh:mm a", date).toString();
            holder.txtDate.setText("SUBMITTED: " + formatted);
        } else {
            holder.txtDate.setText("SUBMITTED: N/A");
        }

        // Status
        if ("complete".equalsIgnoreCase(exam.getStatus()) || "completed".equalsIgnoreCase(exam.getStatus())) {
            holder.txtStatus.setVisibility(View.VISIBLE);
            holder.txtStatus.setText("✔ Completed");
        } else {
            holder.txtStatus.setVisibility(View.GONE);
        }

        // 👉 Handle click
        holder.itemView.setOnClickListener(v -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("examId", exam.getId());
                bundle.putString("subject", exam.getSubject());
                bundle.putDouble("score", exam.getScore());
                bundle.putDouble("total", exam.getTotal());

                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_examhistory_to_takeExamFragment, bundle);

            } catch (Exception e) {
                Log.e("ExamAdapterhistory", "❌ Navigation failed: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubject, txtDate, txtStatus;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}