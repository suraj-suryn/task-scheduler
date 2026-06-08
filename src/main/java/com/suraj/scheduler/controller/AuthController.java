package com.suraj.scheduler.controller;

import com.suraj.scheduler.dto.RegisterDTO;
import com.suraj.scheduler.entity.AppUser;
import com.suraj.scheduler.entity.UserRole;
import com.suraj.scheduler.repository.UserRepository;
import com.suraj.scheduler.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                           BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            model.addAttribute("errorMessage", "Username already taken.");
            return "auth/register";
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            model.addAttribute("errorMessage", "Email already registered.");
            return "auth/register";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "auth/register";
        }

        String token = UUID.randomUUID().toString();

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setVerificationToken(token);

        String verifyUrl = baseUrl + "/verify-email?token=" + token;
        boolean emailSent = notificationService.sendVerificationEmail(
                dto.getEmail(), dto.getUsername(), verifyUrl);

        if (emailSent) {
            // Email sent — user must click link before logging in
            user.setEmailVerified(false);
            userRepository.save(user);
            return "redirect:/register?pending=true&email=" + dto.getEmail();
        } else {
            // SMTP not configured — auto-verify so app still works
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return "redirect:/login?registered=true";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        Optional<AppUser> optUser = userRepository.findByVerificationToken(token);

        if (optUser.isEmpty()) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Invalid or expired verification link.");
            return "auth/verify-email";
        }

        AppUser user = optUser.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        model.addAttribute("success", true);
        model.addAttribute("username", user.getUsername());
        return "auth/verify-email";
    }
}
