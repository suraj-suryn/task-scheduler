package com.suraj.scheduler.service.impl;

import com.suraj.scheduler.entity.AppUser;
import com.suraj.scheduler.entity.Task;
import com.suraj.scheduler.repository.UserRepository;
import com.suraj.scheduler.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final UserRepository userRepository;
    private final Optional<JavaMailSender> mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public NotificationServiceImpl(UserRepository userRepository,
                                   Optional<JavaMailSender> mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Override
    public void sendDueReminder(Task task) {
        String email = resolveEmail(task.getAssignedTo());
        if (email == null) return;

        String subject = "[Task Reminder] \"" + task.getName() + "\" is due soon!";
        String body = "Hi,\n\nYour task \"" + task.getName() + "\" is due by "
                + task.getEndTime() + ".\n\nPlease complete it on time.\n\n— Task Scheduler";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendCompletedNotification(Task task) {
        String email = resolveEmail(task.getAssignedTo());
        if (email == null) return;

        String subject = "[Task Completed] \"" + task.getName() + "\"";
        String body = "Hi,\n\nYour task \"" + task.getName() + "\" has been marked as COMPLETED.\n\n— Task Scheduler";

        sendEmail(email, subject, body);
    }

    @Override
    public boolean sendVerificationEmail(String toEmail, String username, String verificationUrl) {
        if (mailSender.isEmpty() || fromEmail.isBlank()) {
            log.warn("[Notification] SMTP not configured — skipping verification email for {}. Auto-verifying.", username);
            return false;
        }
        String subject = "[Task Scheduler] Verify your email address";
        String body = "Hi " + username + ",\n\n"
                + "Welcome to Task Scheduler! Please verify your email by clicking the link below:\n\n"
                + verificationUrl + "\n\n"
                + "This link will activate your account.\n\n"
                + "If you did not register, ignore this email.\n\n— Task Scheduler";
        sendEmail(toEmail, subject, body);
        return true;
    }

    private void sendEmail(String to, String subject, String body) {
        if (mailSender.isEmpty() || fromEmail.isBlank()) {
            log.warn("[Notification] Email not configured — skipping email to {}. Subject: {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.get().send(message);
            log.info("[Notification] Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.warn("[Notification] Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String resolveEmail(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(AppUser::getEmail)
                .orElse(null);
    }
}
