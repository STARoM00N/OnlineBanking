package com.bankapp.controllers;

import com.bankapp.models.User;
import com.bankapp.repositories.UserRepository;
import com.bankapp.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, Principal principal) {
        // Get username (could be email or username)
        String username = principal.getName();
        model.addAttribute("username", username);

        // Check if it's an email or username and find user accordingly
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));

        // Get user balance
        BigDecimal balance = transactionService.getUserBalance(user.getId());
        model.addAttribute("balance", balance);

        // Add a welcome message
        model.addAttribute("welcomeMessage", "Welcome to your Dashboard!");

        return "dashboard"; // Return the dashboard view
    }

}

