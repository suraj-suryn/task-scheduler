package com.suraj.scheduler.config;

import com.suraj.scheduler.entity.Category;
import com.suraj.scheduler.repository.CategoryRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter implements Converter<String, Category> {

    private final CategoryRepository categoryRepository;

    public CategoryConverter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category convert(String source) {
        if (source == null || source.isBlank()) return null;
        try {
            Long id = Long.parseLong(source);
            return categoryRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
