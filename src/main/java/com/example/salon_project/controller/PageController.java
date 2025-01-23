package com.example.salon_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "Registration";
    }

    @GetMapping("/login")
    public String Showlogin() {
        return "login";
    }

    @GetMapping("/open")
    public String Showopened() {
        return "OpenedSalon";
    }
}
