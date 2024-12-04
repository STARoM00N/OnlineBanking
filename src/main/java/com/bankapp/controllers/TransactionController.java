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

        // ตรวจสอบจำนวนธุรกรรม
        System.out.println("Number of transactions: " + transactions.size());

        // Format each transaction amount
        for (Transaction transaction : transactions) {
            String formattedAmount = formatAmount(transaction.getAmount());
            transaction.setFormattedAmount(formattedAmount);

            // Format date for each transaction
            String formattedDate = formatDate(transaction.getCreatedAt());
            transaction.setFormattedDate(formattedDate); // เก็บวันที่ที่จัดรูปแบบแล้ว
        }

        model.addAttribute("transactions", transactions);
        return "transaction-history";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, Model model, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        transactionService.deposit(userId, amount);
        model.addAttribute("successMessage", "Deposit successful!");
        return "redirect:/dashboard";
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

        // ส่งกลับไปที่หน้า dashboard หลังการถอน
        return "redirect:/dashboard"; // ชื่อเทมเพลตที่คุณต้องการแสดงหลังจากถอน
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
