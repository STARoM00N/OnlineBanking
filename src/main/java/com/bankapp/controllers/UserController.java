package com.bankapp.controllers;

import com.bankapp.models.User;
import com.bankapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Mapping สำหรับ Login
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    // Mapping สำหรับ Register
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // API สำหรับลงทะเบียน
    @PostMapping("/api/users/register")
    public ResponseEntity<?> apiRegisterUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ฟังก์ชันลงทะเบียนผ่านฟอร์ม
    @PostMapping("/register")
    public String formRegisterUser(@RequestParam String name,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam(required = false, defaultValue = "USER") String role,
                                   Model model) {
        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);

            userService.registerUser(user);
            model.addAttribute("successMessage", "Registration successful! You can now log in.");
            return "login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}
