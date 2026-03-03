package com.suraj.scheduler.service;

import com.suraj.scheduler.entity.Task;

import java.util.List;

public interface TaskService {

    Task saveTask(Task task);

    List<Task> getAllTasks();

    void deleteTask(Long id);

    Task getTaskById(Long id);
}
