package com.example.salon_project.controller;

import com.example.salon_project.model.User;
import com.example.salon_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.persistence.*;

import java.util.List;


@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/admins")
    public String showAdminsPage(Model model) {
        List<User> admins = userService.getAllAdmins(); // Получить всех администраторов
        model.addAttribute("admins", admins);
        return "admins";
    }

    @PostMapping("/admins")
    public String addAdmin(@RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        User admin = new User();
        admin.setName(name);
        admin.setPhone(phone);
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setRole("ADMIN");

        userService.saveUser(admin);

        List<User> admins = userService.getAllAdmins();
        model.addAttribute("admins", admins);

        return "admins";
    }
}
