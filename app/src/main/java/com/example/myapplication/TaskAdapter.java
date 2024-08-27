package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final OnTaskClickListener onTaskClickListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener onTaskClickListener) {
        this.tasks = tasks;
        this.onTaskClickListener = onTaskClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);

        // 배경색을 회색으로 설정
        holder.itemView.setBackgroundColor(Color.parseColor("#D3D3D3")); // 회색

        // 항목 간의 간격을 설정합니다.
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        params.bottomMargin = dpToPx(8, holder.itemView.getContext()); // 8dp 간격
        holder.itemView.setLayoutParams(params);

        holder.itemView.setOnClickListener(v -> onTaskClickListener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged(); // 데이터가 변경되었음을 알림
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskNumber;
        TextView taskTime;
        TextView taskName;
        CheckBox taskCheck;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskNumber = itemView.findViewById(R.id.task_number);
            taskTime = itemView.findViewById(R.id.task_time);
            taskName = itemView.findViewById(R.id.task_name);
            taskCheck = itemView.findViewById(R.id.task_check);
        }

        public void bind(Task task) {
            taskName.setText(task.getTask());

            if (task.hasSpecificTime()) {
                taskNumber.setText(String.valueOf(task.getNumber()));
                taskTime.setText(task.getTime() != null ? task.getTime().toString() : ""); // 시간 값이 null일 경우 빈 문자열로 설정
                taskNumber.setVisibility(View.VISIBLE);
                taskTime.setVisibility(View.VISIBLE);
            } else {
                taskNumber.setVisibility(View.GONE);
                taskTime.setVisibility(View.GONE);
            }
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    // dp를 px로 변환하는 메서드
    private int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
