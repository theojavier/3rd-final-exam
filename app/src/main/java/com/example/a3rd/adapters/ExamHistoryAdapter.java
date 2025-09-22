package com.example.a3rd.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.models.ExamHistory;

import java.util.List;

public class ExamHistoryAdapter extends RecyclerView.Adapter<ExamHistoryAdapter.ViewHolder> {

    private final List<ExamHistory> examList;
    private final OnExamClickListener listener;

    // ✅ Functional interface for clicks
    public interface OnExamClickListener {
        void onExamClick(ExamHistory exam);
    }

    public ExamHistoryAdapter(List<ExamHistory> examList, OnExamClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exam_history_item, parent, false); // make sure you have exam_history_item.xml
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamHistory exam = examList.get(position);

        holder.txtSubject.setText(exam.getSubject());
        holder.txtDate.setText(exam.getDate());
        holder.txtStatus.setText(exam.getStatus());

        // ✅ Handle item click
        holder.cardView.setOnClickListener(v -> listener.onExamClick(exam));
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubject, txtDate, txtStatus;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            cardView = (CardView) itemView; // root is CardView
        }
    }
}
