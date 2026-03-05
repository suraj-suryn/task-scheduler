package com.suraj.scheduler.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = com.suraj.scheduler.controller.TaskController.class)
public class UIExceptionHandler {

    @ExceptionHandler(InvalidTaskTimeException.class)
    public String handleInvalidTaskTime(InvalidTaskTimeException ex, Model model) {

        model.addAttribute("errorTitle", "Invalid Task Time");
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    @ExceptionHandler(TaskOverlapException.class)
    public String handleOverlap(TaskOverlapException ex, Model model) {

        model.addAttribute("errorTitle", "Task Overlap");
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

    @ExceptionHandler(DependencyCycleException.class)
    public String handleDependency(DependencyCycleException ex, Model model) {

        model.addAttribute("errorTitle", "Dependency Error");
        model.addAttribute("errorMessage", ex.getMessage());

        return "error";
    }

}
