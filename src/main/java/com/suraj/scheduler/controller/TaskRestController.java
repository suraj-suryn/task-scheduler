package com.suraj.scheduler.controller;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    private final TaskService taskService;

    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/dependencies")
    public Map<Long, List<Long>> getDependencyGraph() {

        List<Task> tasks = taskService.getAllTasks();

        Map<Long, List<Long>> graph = new HashMap<>();

        for (Task task : tasks) {

            if (task.getDependencies() == null || task.getDependencies().isEmpty()) {
                continue;
            }

            String[] deps = task.getDependencies().split(",");

            List<Long> depIds = new ArrayList<>();

            for (String dep : deps) {
                depIds.add(Long.parseLong(dep.trim()));
            }

            graph.put(task.getId(), depIds);
        }

        return graph;
    }
    
}
