package com.suraj.scheduler.controller;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
	public String saveTask(@Valid @ModelAttribute Task task, BindingResult result, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("tasks", taskService.getAllTasks());
			return "tasks";
		}

		taskService.saveTask(task);
		return "redirect:/tasks";
	}

	@GetMapping("/delete/{id}")
	public String deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return "redirect:/tasks";
	}
}
