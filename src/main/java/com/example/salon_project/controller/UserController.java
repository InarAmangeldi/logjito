package com.example.salon_project.controller;

import com.example.salon_project.model.User;
import com.example.salon_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String phone,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(defaultValue = "USER") String role,
                               Model model) {
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        userService.saveUser(user);

        model.addAttribute("name", name);
        model.addAttribute("phone", phone);
        model.addAttribute("email", email);
        model.addAttribute("password", password);
        return "home";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        User user = userService.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin_main"; // нужно что бы при нажатии на него переходил на админа
            } else {
                return "redirect:/home";
            }
        } else {
            model.addAttribute("error", "Неверный email или пароль");
            return "login";
        }
    }



    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }

}
