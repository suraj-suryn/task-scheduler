package com.suraj.scheduler.controller;

import com.suraj.scheduler.entity.AppUser;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.entity.UserRole;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.repository.UserRepository;
import com.suraj.scheduler.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository,
                           TaskService taskService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @GetMapping({"", "/"})
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalTasks", taskRepository.count());
        model.addAttribute("pendingTasks", taskRepository.countByStatus(TaskStatus.PENDING));
        model.addAttribute("completedTasks", taskRepository.countByStatus(TaskStatus.COMPLETED));
        model.addAttribute("failedTasks", taskRepository.countByStatus(TaskStatus.FAILED));
        model.addAttribute("overdueTasks", taskRepository.findAllOverdueTasks(LocalDateTime.now()).size());
        model.addAttribute("recentUsers", userRepository.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam UserRole role,
                             RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("successMessage", "User role updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("successMessage", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/tasks")
    public String manageTasks(Model model,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) TaskStatus status) {
        model.addAttribute("tasks", taskService.getTasksForUser(null, true, search, status, null));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        return "admin/tasks";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Task deleted.");
        return "redirect:/admin/tasks";
    }
}
