package com.bankapp.controllers;

import com.bankapp.services.TransactionService;
import com.bankapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.bankapp.models.Transaction;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

    // Helper method to format BigDecimal with commas
    private String formatAmount(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return ""; // ถ้าไม่มีเวลาให้คืนค่าว่าง
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter); // จัดรูปแบบเวลา
    }

    @GetMapping("/transaction-history")
    public String getTransactionHistory(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        List<Transaction> transactions = transactionService.getTransactionHistory(userId);

        // ตรวจสอบ username และส่งไปยังหน้า transaction history
        String username = userRepository.findById(userId).get().getUsername();
        model.addAttribute("username", username);

        // Format transactions
        for (Transaction transaction : transactions) {
            transaction.setFormattedAmount(formatAmount(transaction.getAmount()));
            transaction.setFormattedDate(formatDate(transaction.getCreatedAt()));
        }

        // เพิ่มยอดเงินคงเหลือ
        BigDecimal balance = transactionService.getUserBalance(userId);
        String formattedBalance = formatAmount(balance);

        model.addAttribute("transactions", transactions);
        model.addAttribute("formattedBalance", formattedBalance); // ส่งยอดเงินคงเหลือ
        return "transaction-history";
    }

    @GetMapping("/deposit")
    public String showDepositPage(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        model.addAttribute("username", userRepository.findById(userId).orElseThrow().getUsername());
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, Model model, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            transactionService.deposit(userId, amount);
            model.addAttribute("successMessage", "Deposit successful!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "deposit"; // แสดงผลใน deposit.html
    }

    @GetMapping("/withdraw")
    public String showWithdrawPage(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        model.addAttribute("username", userRepository.findById(userId).orElseThrow().getUsername());
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            transactionService.withdraw(userId, amount);
            model.addAttribute("successMessage", "Withdrawal successful!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "withdraw"; // แสดงผลใน withdraw.html
    }

    @GetMapping("/transfer")
    public String showTransferPage(Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        model.addAttribute("username", userRepository.findById(userId).orElseThrow().getUsername());
        return "transfer";
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
