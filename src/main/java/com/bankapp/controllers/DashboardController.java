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
        // ดึง username จาก Principal
        String username = principal.getName();
        model.addAttribute("username", username);

        // ดึง User จากฐานข้อมูล
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ดึงยอดเงินคงเหลือ
        BigDecimal balance = transactionService.getUserBalance(user.getId());
        model.addAttribute("balance", balance);

        // เพิ่มข้อความต้อนรับ
        model.addAttribute("welcomeMessage", "Welcome to your Dashboard!");

        return "dashboard"; // ชื่อไฟล์ HTML ที่จะแสดง
    }
}

