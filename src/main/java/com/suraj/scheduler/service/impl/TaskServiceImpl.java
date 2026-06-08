package com.suraj.scheduler.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.suraj.scheduler.dsa.DependencyGraph;
import com.suraj.scheduler.dsa.IntervalTree;
import com.suraj.scheduler.dsa.PriorityHeap;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.exception.DependencyCycleException;
import com.suraj.scheduler.exception.InvalidTaskTimeException;
import com.suraj.scheduler.exception.TaskNotFoundException;
import com.suraj.scheduler.exception.TaskOverlapException;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.service.TaskService;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final IntervalTree intervalTree = new IntervalTree();
    private final DependencyGraph dependencyGraph = new DependencyGraph();
    private final PriorityHeap priorityHeap = new PriorityHeap();

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    public void hydrateDsaStructures() {
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            intervalTree.insert(task);
            if (task.getStatus() == TaskStatus.PENDING) {
                priorityHeap.add(task);
            }
        }
    }

    @Override
    public Task saveTask(Task task) {

        if (task.getEndTime().isBefore(task.getStartTime())) {
            throw new InvalidTaskTimeException("End time must be after start time");
        }

        // Exclude the task itself when checking overlaps (for updates)
        List<Task> others = taskRepository.findAll().stream()
                .filter(t -> !t.getId().equals(task.getId()))
                .toList();

        boolean overlap = intervalTree.isOverlapping(task.getStartTime(), task.getEndTime(), others);
        if (overlap) {
            throw new TaskOverlapException("Task time overlaps with an existing task");
        }

        boolean cycle = dependencyGraph.hasCycle(task, others);
        if (cycle) {
            throw new DependencyCycleException("Dependency cycle detected");
        }

        priorityHeap.add(task);
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getTasksForUser(Long userId, boolean isAdmin,
                                      String search, TaskStatus status, Long categoryId) {
        if (isAdmin) {
            return taskRepository.searchAllTasks(
                    (search != null && !search.isBlank()) ? search : null,
                    status,
                    categoryId);
        }
        return taskRepository.searchTasks(userId,
                (search != null && !search.isBlank()) ? search : null,
                status,
                categoryId);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }

    @Override
    public Task updateTask(Long id, Task updated) {
        Task existing = getTaskById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPriority(updated.getPriority());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setDependencies(updated.getDependencies());
        existing.setCategory(updated.getCategory());
        existing.setRecurrenceType(updated.getRecurrenceType());
        existing.setRecurrenceEndDate(updated.getRecurrenceEndDate());
        return saveTask(existing);
    }

    @Override
    public Task updateStatus(Long id, TaskStatus status) {
        Task task = getTaskById(id);
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return taskRepository.save(task);
    }
}
