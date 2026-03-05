package com.suraj.scheduler.scheduler;

import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.entity.TaskStatus;
import com.suraj.scheduler.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TaskSchedulerEngine {

    private final TaskRepository taskRepository;

    public TaskSchedulerEngine(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRate = 10000)
    public void executeTasks() {

        List<Task> tasks = taskRepository.findAll();

        for (Task task : tasks) {

            if (task.getStatus() == TaskStatus.PENDING &&
                    task.getStartTime().isBefore(LocalDateTime.now())) {

                System.out.println("Executing task: " + task.getName());

                task.setStatus(TaskStatus.RUNNING);
                taskRepository.save(task);

                try {

                    Thread.sleep(2000);

                    task.setStatus(TaskStatus.COMPLETED);

                } catch (Exception e) {

                    task.setStatus(TaskStatus.FAILED);

                }

                taskRepository.save(task);
            }
        }
    }
}
