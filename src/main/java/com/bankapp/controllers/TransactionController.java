package com.bankapp.controllers;

import com.bankapp.repositories.UserRepository;
import com.bankapp.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.bankapp.models.Transaction;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    private Long getUserIdFromPrincipal(Principal principal) {
        return userRepository.findByEmailOrUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping("/deposit")
    public String showDepositPage() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        transactionService.deposit(userId, amount);
        model.addAttribute("successMessage", "Deposit successful!");
        return "redirect:/dashboard";
    }

    @GetMapping("/transaction-history")
    public String getTransactionHistory(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        List<Transaction> transactions = transactionService.getTransactionHistory(userId);
        model.addAttribute("transactions", transactions);
        return "transaction-history";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            transactionService.withdraw(userId, amount); // เรียกฟังก์ชัน withdraw
            model.addAttribute("successMessage", "Withdrawal successful!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage()); // แสดงข้อผิดพลาดถ้ามี
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam Long receiverId, @RequestParam BigDecimal amount, Model model, Principal principal) {
        Long senderId = getUserIdFromPrincipal(principal);
        try {
            transactionService.transfer(senderId, receiverId, amount);
            model.addAttribute("successMessage", "Transfer successful!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}