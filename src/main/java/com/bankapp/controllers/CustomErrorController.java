package com.bankapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController {

    @RequestMapping("/error")
    public String handleError(Model model) {
        model.addAttribute("error", "Oops! Something went wrong.");
        return "error-page";
    }

}
