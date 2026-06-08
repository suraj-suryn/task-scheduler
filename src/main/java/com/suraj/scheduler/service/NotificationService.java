package com.suraj.scheduler.service;

import com.suraj.scheduler.entity.Task;

public interface NotificationService {
    void sendDueReminder(Task task);
    void sendCompletedNotification(Task task);
    /**
     * Returns true if email was sent, false if SMTP not configured (caller should auto-verify).
     */
    boolean sendVerificationEmail(String toEmail, String username, String verificationUrl);
}
