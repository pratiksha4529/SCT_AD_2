package com.example.todolist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime; // New Field for display
    private boolean isCompleted;
    private String category;
    private long alarmTimestamp; // New Field for logic

    public Task(String title, String description, String dueDate, String dueTime, boolean isCompleted, String category, long alarmTimestamp) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isCompleted = isCompleted;
        this.category = category;
        this.alarmTimestamp = alarmTimestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getAlarmTimestamp() { return alarmTimestamp; }
    public void setAlarmTimestamp(long alarmTimestamp) { this.alarmTimestamp = alarmTimestamp; }
}