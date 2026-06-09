package com.suraj.scheduler.controller;

import com.suraj.scheduler.common.ApiResponse;
import com.suraj.scheduler.dto.DashboardStats;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.security.SecurityUtils;
import com.suraj.scheduler.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Tasks API", description = "Task management and scheduling endpoints")
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
    @Operation(summary = "Get task dependency graph", description = "Returns a map of taskId -> list of dependency task IDs")
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

    @GetMapping("/tasks/export/csv")
    @Operation(summary = "Export tasks as CSV", description = "Downloads all tasks for the current user as a CSV file")
    public void exportCsv(HttpServletResponse response) throws IOException {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        List<Task> tasks = taskService.getTasksForUser(userId, isAdmin, null, null, null);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"tasks-" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")) + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Name,Description,Priority,Status,Category,Start Time,End Time,Recurrence");
        for (Task t : tasks) {
            writer.printf("%d,\"%s\",\"%s\",%d,%s,\"%s\",%s,%s,%s%n",
                    t.getId(),
                    escape(t.getName()),
                    escape(t.getDescription()),
                    t.getPriority(),
                    t.getStatus(),
                    t.getCategory() != null ? escape(t.getCategory().getName()) : "",
                    t.getStartTime() != null ? t.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    t.getEndTime() != null ? t.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    t.getRecurrenceType() != null ? t.getRecurrenceType() : "NONE"
            );
        }
        writer.flush();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    @GetMapping("/tasks/suggest")
    @Operation(summary = "Suggest next task", description = "Returns the highest-priority pending task whose dependencies are all completed")
    public ResponseEntity<ApiResponse<Task>> suggestNextTask() {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        List<Task> tasks = taskService.getTasksForUser(userId, isAdmin, null, null, null);

        // Collect completed task IDs
        Set<Long> completedIds = new HashSet<>();
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) completedIds.add(t.getId());
        }

        // Find highest-priority PENDING task whose all dependencies are completed
        Task best = null;
        for (Task t : tasks) {
            if (t.getStatus() != TaskStatus.PENDING) continue;
            boolean depsOk = true;
            if (t.getDependencies() != null && !t.getDependencies().isBlank()) {
                for (String dep : t.getDependencies().split(",")) {
                    try {
                        long depId = Long.parseLong(dep.trim());
                        if (!completedIds.contains(depId)) { depsOk = false; break; }
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (depsOk) {
                if (best == null || t.getPriority() > best.getPriority()) best = t;
            }
        }

        if (best == null) return ResponseEntity.ok(ApiResponse.success(null));
        return ResponseEntity.ok(ApiResponse.success(best));
    }
}
