package com.suraj.scheduler.service.impl;

import com.suraj.scheduler.entity.Category;
import com.suraj.scheduler.exception.TaskNotFoundException;
import com.suraj.scheduler.repository.CategoryRepository;
import com.suraj.scheduler.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category save(Category category, Long userId) {
        category.setCreatedBy(userId);
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAll(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByCreatedByOrderByNameAsc(userId);
    }

    @Override
    public void delete(Long id, Long userId, boolean isAdmin) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Category not found"));
        if (!isAdmin && !cat.getCreatedBy().equals(userId)) {
            throw new SecurityException("Not authorized to delete this category");
        }
        categoryRepository.delete(cat);
    }

    @Override
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Category not found"));
    }
}
