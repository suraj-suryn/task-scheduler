package com.suraj.scheduler.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TaskDTO {
    private String name;
    private int priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> dependencies; // IDs only

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<Long> getDependencies() { return dependencies; }
    public void setDependencies(List<Long> dependencies) { this.dependencies = dependencies; }
}
