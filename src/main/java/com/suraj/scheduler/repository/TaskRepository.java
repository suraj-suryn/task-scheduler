package com.suraj.scheduler.repository;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByPriorityDesc();

    List<Task> findAllByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Task> findByAssignedToOrderByPriorityDesc(Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByAssignedToAndStatus(Long userId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId " +
           "AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId)")
    List<Task> searchTasks(@Param("userId") Long userId,
                           @Param("search") String search,
                           @Param("status") TaskStatus status,
                           @Param("categoryId") Long categoryId);

    @Query("SELECT t FROM Task t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId)")
    List<Task> searchAllTasks(@Param("search") String search,
                               @Param("status") TaskStatus status,
                               @Param("categoryId") Long categoryId);

    long countByAssignedToAndStatus(Long userId, TaskStatus status);

    long countByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :userId AND t.status = 'PENDING' AND t.endTime < :now")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.endTime < :now")
    List<Task> findAllOverdueTasks(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.endTime BETWEEN :start AND :end")
    List<Task> findTasksDueSoon(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Task> findTop5ByAssignedToAndStatusOrderByCompletedAtDesc(Long userId, TaskStatus status);

    List<Task> findTop5ByAssignedToAndStatusOrderByStartTimeAsc(Long userId, TaskStatus status);
}
