package com.suraj.scheduler.service;

import com.suraj.scheduler.entity.Task;

public interface NotificationService {
    void sendDueReminder(Task task);
    void sendCompletedNotification(Task task);
}
