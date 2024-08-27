package com.example.myapplication;

import java.time.LocalDate;
import java.time.LocalTime;

public class Task {
    private String id; // ID를 String으로 변경
    private String task;
    private LocalTime time;
    private boolean hasSpecificTime;
    private int number;
    private LocalDate date;

    public Task(String id, String task, LocalTime time, boolean hasSpecificTime, int number, LocalDate date) {
        this.id = id;
        this.task = task;
        this.time = time;
        this.hasSpecificTime = hasSpecificTime;
        this.number = number;
        this.date = date;
    }

    // Getter와 Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public boolean hasSpecificTime() { return hasSpecificTime; }
    public void setHasSpecificTime(boolean hasSpecificTime) { this.hasSpecificTime = hasSpecificTime; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
