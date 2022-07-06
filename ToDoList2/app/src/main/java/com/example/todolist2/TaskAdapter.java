package com.example.todolist2;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    private List<Task> tasks = new ArrayList<>();

    private OnTaskClickListener listener;
    private OnDoneButtonClickListener buttonListener;
    private OnAttachmentClickListener attachmentListener;

    public void setFilteredList(List<Task> filteredList){
        this.tasks = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.task_item, viewGroup, false);
        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder taskHolder, int i) {
        Task currentTask = tasks.get(i);
        taskHolder.title.setText(currentTask.getTitle());
        taskHolder.category.setText(currentTask.getCategory());
        taskHolder.description.setText(currentTask.getDescription());
        taskHolder.startDate.setText(currentTask.getStart());
        taskHolder.endDate.setText(currentTask.getEnd());

        if(currentTask.getFilePath().equals("")){
            taskHolder.attachment.setVisibility(View.GONE);
        } else {
            taskHolder.attachment.setVisibility(View.VISIBLE);
        }

        if (currentTask.getFinished()) {
            taskHolder.isDone.setVisibility(View.VISIBLE);
            taskHolder.endDate.setTextColor(Color.parseColor("#2DB533"));
        } else {
            taskHolder.isDone.setVisibility(View.INVISIBLE);
            taskHolder.endDate.setTextColor(Color.parseColor("#FFFFFFFF"));
        }

        String[] endDateSplitted = currentTask.getEnd().split("-");
        boolean isAfterDeadline = isAfterDeadline(Integer.parseInt(endDateSplitted[0]), Integer.parseInt(endDateSplitted[1]), Integer.parseInt(endDateSplitted[2]));
        if(isAfterDeadline){
            taskHolder.endDate.setTextColor(Color.parseColor("#EA1611"));
        }

        if(currentTask.getFinished()){
            taskHolder.endDate.setTextColor(Color.parseColor("#2DB533"));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public Task getTask(int position) {
        return tasks.get(position);
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView description;
        private TextView category;
        private TextView startDate;
        private TextView endDate;
        private ImageView isDone;
        private Button doneButton;
        private ImageButton attachment;

        public TaskHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            category = itemView.findViewById(R.id.category);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
            isDone = itemView.findViewById(R.id.isDoneCheck);
            doneButton = itemView.findViewById(R.id.checkButton);
            attachment = itemView.findViewById(R.id.attachmentButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onTaskClick(tasks.get(position));
                    }
                }
            });

            doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (buttonListener != null && position != RecyclerView.NO_POSITION) {
                        buttonListener.onDoneButtonClick(tasks.get(position));
                    }
                }
            });

            attachment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (attachmentListener != null && position != RecyclerView.NO_POSITION) {
                        attachmentListener.onAttachmentClick(tasks.get(position));
                    }
                }
            });
        }
    }

    private boolean isAfterDeadline(int day, int month, int year){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        if(currentYear > year || currentMonth > month || currentDay > day){
            return true;
        }
        return false;
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public interface OnDoneButtonClickListener{
        void onDoneButtonClick(Task task);
    }

    public void setOnDoneButtonClickListener(OnDoneButtonClickListener buttonListener){
        this.buttonListener = buttonListener;
    }

    public interface OnAttachmentClickListener{
        void onAttachmentClick(Task task);
    }

    public void setOnAttachmentClickListener(OnAttachmentClickListener attachmentListener){
        this.attachmentListener = attachmentListener;
    }
}
