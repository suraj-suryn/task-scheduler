package com.suraj.scheduler.controller;

import com.suraj.scheduler.dto.DashboardStats;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class DashboardController {

    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public DashboardController(TaskRepository taskRepository, SecurityUtils securityUtils) {
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
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
            stats.setRecentCompleted(taskRepository.findTop5ByAssignedToAndStatusOrderByCompletedAtDesc(
                    userId, TaskStatus.COMPLETED));
            stats.setUpcomingTasks(taskRepository.findTop5ByAssignedToAndStatusOrderByStartTimeAsc(
                    userId, TaskStatus.PENDING));
        } else {
            stats.setTotal(taskRepository.findByAssignedToOrderByPriorityDesc(userId).size());
            stats.setPending(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.PENDING));
            stats.setRunning(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.RUNNING));
            stats.setCompleted(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.COMPLETED));
            stats.setFailed(taskRepository.countByAssignedToAndStatus(userId, TaskStatus.FAILED));
            stats.setOverdue(taskRepository.findOverdueTasks(userId, LocalDateTime.now()).size());
            stats.setRecentCompleted(taskRepository.findTop5ByAssignedToAndStatusOrderByCompletedAtDesc(
                    userId, TaskStatus.COMPLETED));
            stats.setUpcomingTasks(taskRepository.findTop5ByAssignedToAndStatusOrderByStartTimeAsc(
                    userId, TaskStatus.PENDING));
        }

        model.addAttribute("stats", stats);
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        return "dashboard";
    }
}
