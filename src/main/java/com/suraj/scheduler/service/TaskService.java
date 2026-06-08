package com.suraj.scheduler.service;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;

import java.util.List;

public interface TaskService {

    Task saveTask(Task task);

    List<Task> getAllTasks();

    List<Task> getTasksForUser(Long userId, boolean isAdmin,
                               String search, TaskStatus status, Long categoryId);

    void deleteTask(Long id);

    Task getTaskById(Long id);

    Task updateTask(Long id, Task updated);

    Task updateStatus(Long id, TaskStatus status);
}
