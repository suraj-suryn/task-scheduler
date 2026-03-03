package com.suraj.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.suraj.scheduler.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Optional: Find tasks by priority (descending)
    List<Task> findAllByOrderByPriorityDesc();

    // Optional: Find tasks by start time
    List<Task> findAllByStartTimeBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
