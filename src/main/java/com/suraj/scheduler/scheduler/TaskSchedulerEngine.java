package com.suraj.scheduler.scheduler;

import com.suraj.scheduler.entity.RecurrenceType;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.repository.TaskRepository;
import com.suraj.scheduler.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TaskSchedulerEngine {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskSchedulerEngine(TaskRepository taskRepository,
                               NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void executeTasks() {
        List<Task> tasks = taskRepository.findByStatus(TaskStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {
            if (!task.getEndTime().isBefore(now)) continue;

            System.out.println("[Scheduler] Executing task: " + task.getName());

            task.setStatus(TaskStatus.RUNNING);
            taskRepository.save(task);

            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            handleRecurrence(task);
        }
    }

    private void handleRecurrence(Task completed) {
        if (completed.getRecurrenceType() == null
                || completed.getRecurrenceType() == RecurrenceType.NONE) {
            return;
        }

        LocalDate endDate = completed.getRecurrenceEndDate();
        if (endDate != null && LocalDate.now().isAfter(endDate)) {
            return;
        }

        Task next = new Task();
        next.setName(completed.getName());
        next.setDescription(completed.getDescription());
        next.setPriority(completed.getPriority());
        next.setDependencies(completed.getDependencies());
        next.setCategory(completed.getCategory());
        next.setRecurrenceType(completed.getRecurrenceType());
        next.setRecurrenceEndDate(completed.getRecurrenceEndDate());
        next.setAssignedTo(completed.getAssignedTo());
        next.setStatus(TaskStatus.PENDING);

        switch (completed.getRecurrenceType()) {
            case DAILY:
                next.setStartTime(completed.getStartTime().plusDays(1));
                next.setEndTime(completed.getEndTime().plusDays(1));
                break;
            case WEEKLY:
                next.setStartTime(completed.getStartTime().plusWeeks(1));
                next.setEndTime(completed.getEndTime().plusWeeks(1));
                break;
            case MONTHLY:
                next.setStartTime(completed.getStartTime().plusMonths(1));
                next.setEndTime(completed.getEndTime().plusMonths(1));
                break;
            default:
                break;
        }

        taskRepository.save(next);
        System.out.println("[Scheduler] Recurring task created: " + next.getName()
                + " -> " + next.getStartTime());
    }

    // Daily 8 AM reminder for tasks due today
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(24);
        List<Task> dueSoon = taskRepository.findTasksDueSoon(start, end);

        for (Task task : dueSoon) {
            System.out.println("[Reminder] Task due soon: " + task.getName());
            notificationService.sendDueReminder(task);
        }
    }
}
