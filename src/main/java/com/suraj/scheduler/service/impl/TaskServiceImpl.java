package com.suraj.scheduler.service.impl;

import com.suraj.scheduler.dsa.DependencyGraph;
import com.suraj.scheduler.dsa.IntervalTree;
import com.suraj.scheduler.dsa.PriorityHeap;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.exception.DependencyCycleException;
import com.suraj.scheduler.exception.TaskNotFoundException;
import com.suraj.scheduler.exception.TaskOverlapException;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final IntervalTree intervalTree = new IntervalTree();
    private final DependencyGraph dependencyGraph = new DependencyGraph();
    private final PriorityHeap priorityHeap = new PriorityHeap();

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task saveTask(Task task) {

        // 1️⃣ Check time overlap
        boolean overlap = intervalTree.isOverlapping(
                task.getStartTime(),
                task.getEndTime(),
                taskRepository.findAll()
        );

        if (overlap) {
            throw new TaskOverlapException("Task time overlaps with existing task");
        }

        // 2️⃣ Check dependency cycle
        boolean cycle = dependencyGraph.hasCycle(task, taskRepository.findAll());

        if (cycle) {
            throw new DependencyCycleException("Dependency cycle detected");
        }

        // 3️⃣ Add to priority heap
        priorityHeap.add(task);

        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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
}
