package com.suraj.scheduler.controller;

import com.suraj.scheduler.common.ApiResponse;
import com.suraj.scheduler.dto.DashboardStats;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.security.SecurityUtils;
import com.suraj.scheduler.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TaskRestController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public TaskRestController(TaskService taskService, TaskRepository taskRepository,
                              SecurityUtils securityUtils) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        List<Task> tasks = taskService.getTasksForUser(userId, isAdmin, search, status, categoryId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Task>> getTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody Task task) {
        task.setAssignedTo(securityUtils.getCurrentUserId());
        Task saved = taskService.saveTask(task);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Long id,
                                                         @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<ApiResponse<Task>> updateStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        TaskStatus status = TaskStatus.valueOf(body.get("status").toUpperCase());
        Task updated = taskService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats() {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();

        DashboardStats stats = new DashboardStats();
        if (isAdmin) {
            stats.setTotal(taskRepository.count());
            stats.setPending(taskRepository.countByStatus(TaskStatus.PENDING));
            stats.setRunning(taskRepository.countByStatus(TaskStatus.RUNNING));
            stats.setCompleted(taskRepository.countByStatus(TaskStatus.COMPLETED));
            stats.setFailed(taskRepository.countByStatus(TaskStatus.FAILED));
            stats.setOverdue(taskRepository.findAllOverdueTasks(LocalDateTime.now()).size());
        } else {
            stats.setTotal(taskRepository.findByAssignedToOrderByPriorityDesc(userId).size());
            stats.setPending(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.PENDING));
            stats.setRunning(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.RUNNING));
            stats.setCompleted(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.COMPLETED));
            stats.setFailed(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.FAILED));
            stats.setOverdue(taskRepository.findOverdueTasks(userId, LocalDateTime.now()).size());
        }

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/tasks/dependencies")
    public ResponseEntity<ApiResponse<Map<Long, List<Long>>>> getDependencyGraph() {
        List<Task> tasks = taskService.getAllTasks();
        Map<Long, List<Long>> graph = new HashMap<>();

        for (Task task : tasks) {
            if (task.getDependencies() == null || task.getDependencies().isEmpty()) continue;
            List<Long> depIds = new ArrayList<>();
            for (String dep : task.getDependencies().split(",")) {
                try { depIds.add(Long.parseLong(dep.trim())); } catch (NumberFormatException ignored) {}
            }
            graph.put(task.getId(), depIds);
        }

        return ResponseEntity.ok(ApiResponse.success(graph));
    }
}
