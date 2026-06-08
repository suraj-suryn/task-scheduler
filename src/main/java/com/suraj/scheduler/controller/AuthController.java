package com.suraj.scheduler.controller;

import com.suraj.scheduler.dto.RegisterDTO;
import com.suraj.scheduler.entity.AppUser;
import com.suraj.scheduler.entity.UserRole;
import com.suraj.scheduler.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.ROLE_USER);

        userRepository.save(user);

        return "redirect:/login?registered=true";
    }
}
