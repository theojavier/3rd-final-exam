package com.example.a3rd.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.models.ExamHistory;

import java.util.List;

public class ExamHistoryAdapter extends RecyclerView.Adapter<ExamHistoryAdapter.ViewHolder> {

    private final List<ExamHistory> historyList;

    public ExamHistoryAdapter(List<ExamHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exam_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamHistory history = historyList.get(position);

        holder.txtSubject.setText(history.getSubject());
        holder.txtDate.setText(history.getDate());
        holder.txtScore.setText(history.getScore());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubject, txtDate, txtScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtScore = itemView.findViewById(R.id.txtScore);
        }
    }
}