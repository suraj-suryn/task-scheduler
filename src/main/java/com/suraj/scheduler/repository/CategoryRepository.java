package com.suraj.scheduler.repository;

import com.suraj.scheduler.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByCreatedByOrderByNameAsc(Long createdBy);
}
