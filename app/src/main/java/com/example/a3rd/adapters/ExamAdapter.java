package com.example.a3rd.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;
import com.example.a3rd.models.ExamModel;
import com.example.a3rd.ui.exam.TakeExamFragment;

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
        holder.tvLoginTime.setText("LOGIN TIME: " + exam.getFormattedLoginTime());
        holder.tvPosted.setText("Posted " + exam.getFormattedPostedDate());

        if ("Complete".equalsIgnoreCase(exam.getStatus())) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("✔ Complete");
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // 👉 When user clicks, go to TakeExamActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TakeExamFragment.class);
            intent.putExtra("subject", exam.getSubject());
            intent.putExtra("teacherId", exam.getTeacherId());
            intent.putExtra("startTime", exam.getStartTime().toDate().getTime());
            intent.putExtra("endTime", exam.getEndTime().toDate().getTime());
            context.startActivity(intent);
        });
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
