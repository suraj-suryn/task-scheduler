package com.suraj.scheduler.controller;

import com.suraj.scheduler.entity.Category;
import com.suraj.scheduler.security.SecurityUtils;
import com.suraj.scheduler.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    public CategoryController(CategoryService categoryService, SecurityUtils securityUtils) {
        this.categoryService = categoryService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String list(Model model) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        model.addAttribute("categories", categoryService.getAll(userId, isAdmin));
        model.addAttribute("category", new Category());
        return "categories";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") Category category,
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Long userId = securityUtils.getCurrentUserId();
            boolean isAdmin = securityUtils.isAdmin();
            model.addAttribute("categories", categoryService.getAll(userId, isAdmin));
            return "categories";
        }
        Long userId = securityUtils.getCurrentUserId();
        categoryService.save(category, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully!");
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        categoryService.delete(id, userId, isAdmin);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted.");
        return "redirect:/categories";
    }
}
