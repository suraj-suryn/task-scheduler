package com.suraj.scheduler.config;

import com.suraj.scheduler.entity.AppUser;
import com.suraj.scheduler.entity.UserRole;
import com.suraj.scheduler.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setEmail("admin@taskscheduler.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ROLE_ADMIN);
            userRepository.save(admin);

            System.out.println("============================================================");
            System.out.println("  DEFAULT ADMIN CREATED: username=admin  password=admin123");
            System.out.println("  IMPORTANT: Change the admin password after first login!  ");
            System.out.println("============================================================");
        }
    }
}
