package com.suraj.scheduler.service;

import com.suraj.scheduler.entity.Category;

import java.util.List;

public interface CategoryService {

    Category save(Category category, Long userId);

    List<Category> getAll(Long userId, boolean isAdmin);

    void delete(Long id, Long userId, boolean isAdmin);

    Category getById(Long id);
}
