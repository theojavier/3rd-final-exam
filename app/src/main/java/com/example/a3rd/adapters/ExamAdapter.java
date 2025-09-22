package com.example.a3rd.adapters;

import android.content.Context;
import android.os.Bundle;
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
        holder.tvLoginTime.setText("LOGIN TIME: " + exam.getFormattedLoginTime());
        holder.tvPosted.setText("Posted " + exam.getFormattedPostedDate());

        if ("Complete".equalsIgnoreCase(exam.getStatus())) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("✔ Complete");
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("examId", exam.getId());
                bundle.putString("subject", exam.getSubject());
                bundle.putString("teacherId", exam.getTeacherId());

                if (exam.getStartTime() != null) {
                    bundle.putLong("startTime", exam.getStartTime().toDate().getTime());
                }
                if (exam.getEndTime() != null) {
                    bundle.putLong("endTime", exam.getEndTime().toDate().getTime());
                }

                NavController navController = Navigation.findNavController(v);
                int currentDestId = navController.getCurrentDestination().getId();

                if (currentDestId == R.id.nav_exam_item_page) {
                    navController.navigate(R.id.action_examListFragment_to_takeExamFragment, bundle);
                } else if (currentDestId == R.id.nav_exam_history) {
                    navController.navigate(R.id.action_examhistory_to_takeExamFragment, bundle);
                } else {
                    Log.e("ExamAdapter", "Unexpected destination: " + currentDestId);
                }

            } catch (Exception e) {
                Log.e("ExamAdapter", "Navigation failed: " + e.getMessage(), e);
            }
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