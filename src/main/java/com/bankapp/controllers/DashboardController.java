package com.bankapp.controllers;

import com.bankapp.models.User;
import com.bankapp.repositories.UserRepository;
import com.bankapp.services.TransactionService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.logging.Logger;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/dashboard")
    public String dashboardPage(Model model, Principal principal) {
        // Retrieve username from Principal
        String usernameOrEmail = principal.getName();

        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElse(null);

        if (user == null) {
            model.addAttribute("errorMessage", "User not found.");
            return "error-page";
        }

        // Get user's balance
        BigDecimal balance = transactionService.getUserBalance(user.getId());
        // Format the balance
        DecimalFormat df = new DecimalFormat("#,###.00");
        String formattedBalance = df.format(balance);

        // Get total income and expenses for the user
        BigDecimal income = transactionService.getTotalIncome(user.getId());
        BigDecimal expenses = transactionService.getTotalExpenses(user.getId());

        // Format the income and expenses
        String formattedIncome = df.format(income);
        String formattedExpenses = df.format(expenses);

        // Pass data to the view
        model.addAttribute("formattedBalance", formattedBalance);
        model.addAttribute("formattedIncome", formattedIncome);
        model.addAttribute("formattedExpenses", formattedExpenses);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("income", income); // For chart
        model.addAttribute("expenses", expenses); // For chart

        return "dashboard";
    }
}