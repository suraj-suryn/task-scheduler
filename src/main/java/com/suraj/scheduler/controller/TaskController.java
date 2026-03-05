package com.suraj.scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.exception.DependencyCycleException;
import com.suraj.scheduler.exception.TaskOverlapException;
import com.suraj.scheduler.service.TaskService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping
	public String listTasks(Model model) {
		model.addAttribute("tasks", taskService.getAllTasks());
		model.addAttribute("task", new Task());
		return "tasks";
	}

	@PostMapping("/save")
	public String saveTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model) {

		 // Validation errors from @Valid
	    if (result.hasErrors()) {
	        model.addAttribute("tasks", taskService.getAllTasks());
	        model.addAttribute("errorMessage", "Validation failed. Please correct the fields.");
	        return "tasks";
	    }

	    try {

	        taskService.saveTask(task);

	    } catch (TaskOverlapException e) {

	        model.addAttribute("errorMessage", e.getMessage());
	        model.addAttribute("tasks", taskService.getAllTasks());
	        return "tasks";

	    } catch (DependencyCycleException e) {

	        model.addAttribute("errorMessage", e.getMessage());
	        model.addAttribute("tasks", taskService.getAllTasks());
	        return "tasks";

	    } catch (Exception e) {

	        model.addAttribute("errorMessage", "Unexpected error occurred");
	        model.addAttribute("tasks", taskService.getAllTasks());
	        return "tasks";
	    }
		taskService.saveTask(task);
		return "redirect:/";
	}

	@GetMapping("/delete/{id}")
	public String deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return "redirect:/tasks";
	}
}
