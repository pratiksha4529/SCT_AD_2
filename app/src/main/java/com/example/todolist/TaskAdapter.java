package com.example.todolist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
        void onCheckClick(int position, boolean isChecked);
    }

    public TaskAdapter(List<Task> taskList, OnItemClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.title.setText(task.getTitle());
// Change this line:
        holder.date.setText(task.getDueDate() + " " + (task.getDueTime() != null ? task.getDueTime() : ""));
        // Remove listener temporarily to avoid triggering it during recycling
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        // Add listeners
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
        holder.itemView.setOnClickListener(v -> listener.onEditClick(position)); // Edit on tap
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onCheckClick(position, isChecked));
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        CheckBox checkBox;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            date = itemView.findViewById(R.id.textDate);
            checkBox = itemView.findViewById(R.id.checkBoxCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}