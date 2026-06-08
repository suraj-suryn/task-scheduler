package com.suraj.scheduler.dto;

import com.suraj.scheduler.entity.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardStats {

    private long total;
    private long pending;
    private long running;
    private long completed;
    private long failed;
    private long overdue;

    private List<Task> recentCompleted;
    private List<Task> upcomingTasks;
}
