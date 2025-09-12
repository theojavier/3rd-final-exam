package com.example.a3rd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.models.ExamModel;

import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private Context context;
    private List<ExamModel> examList;

    public ExamAdapter(Context context, List<ExamModel> examList) {
        this.context = context;
        this.examList = examList;
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        ExamModel exam = examList.get(position);

        holder.tvSubject.setText(exam.getSubject());
        holder.tvLoginTime.setText("LOGIN TIME: " + exam.getLoginTime());
        holder.tvPosted.setText("Posted " + exam.getPostedDate());

        // Show DONE only if exam is completed
        if (exam.isDone()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("✔ DONE");
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvStatus, tvLoginTime, tvPosted;

        public ExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvLoginTime = itemView.findViewById(R.id.tv_login_time);
            tvPosted = itemView.findViewById(R.id.tv_posted);
        }
    }
}
