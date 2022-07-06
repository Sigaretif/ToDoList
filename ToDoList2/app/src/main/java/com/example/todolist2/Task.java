package com.example.todolist2;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "task_table")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private String category;

    private String description;

    private String start;

    private String end;

    private Boolean isFinished;

    private String filePath;

    public Task(String title, String category, String description, String start, String end, Boolean isFinished, String filePath) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.start = start;
        this.end = end;
        this.isFinished = isFinished;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public Boolean getFinished() {
        return isFinished;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

