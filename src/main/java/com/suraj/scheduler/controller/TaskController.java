package com.suraj.scheduler.controller;

import com.suraj.scheduler.entity.RecurrenceType;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.exception.DependencyCycleException;
import com.suraj.scheduler.exception.TaskOverlapException;
import com.suraj.scheduler.security.SecurityUtils;
import com.suraj.scheduler.service.CategoryService;
import com.suraj.scheduler.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    public TaskController(TaskService taskService, CategoryService categoryService,
                          SecurityUtils securityUtils) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String listTasks(Model model,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) TaskStatus status,
                            @RequestParam(required = false) Long categoryId) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();

        model.addAttribute("tasks", taskService.getTasksForUser(userId, isAdmin, search, status, categoryId));
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryService.getAll(userId, isAdmin));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("recurrenceTypes", RecurrenceType.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategoryId", categoryId);
        return "tasks";
    }

    @PostMapping("/save")
    public String saveTask(@Valid @ModelAttribute("task") Task task,
                           BindingResult result, Model model,
                           RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();

        if (result.hasErrors()) {
            model.addAttribute("tasks", taskService.getTasksForUser(userId, isAdmin, null, null, null));
            model.addAttribute("categories", categoryService.getAll(userId, isAdmin));
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("recurrenceTypes", RecurrenceType.values());
            return "tasks";
        }

        task.setAssignedTo(userId);

        try {
            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("successMessage", "Task created successfully!");
        } catch (TaskOverlapException | DependencyCycleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
        }

        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("successMessage", "Task deleted.");
        return "redirect:/tasks";
    }

    @GetMapping("/graph")
    public String dependencyGraph(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        model.addAttribute("tasks", taskService.getTasksForUser(userId, isAdmin, null, null, null));
        return "tasks-graph";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @Valid @ModelAttribute("task") Task task,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();

        if (result.hasErrors()) {
            model.addAttribute("tasks", taskService.getTasksForUser(userId, isAdmin, null, null, null));
            model.addAttribute("categories", categoryService.getAll(userId, isAdmin));
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("recurrenceTypes", RecurrenceType.values());
            return "tasks";
        }

        try {
            taskService.updateTask(id, task);
            redirectAttributes.addFlashAttribute("successMessage", "Task updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tasks";
    }
}
