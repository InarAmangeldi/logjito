package com.example.salon_project.controller;

import com.example.salon_project.model.Salon;
import com.example.salon_project.model.User;
import com.example.salon_project.request.CategoryRequest;
import com.example.salon_project.service.SalonService;
import com.example.salon_project.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

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
                               Model model,
                               HttpSession session
    ) {
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
        session.setAttribute("userId", user.getId());
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session
    ) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                if ("ADMIN".equals(user.getRole())) {
                    session.setAttribute("adminId", user.getId());
                    if (salonService.existsByAdminId(user.getId())) {
                        return "redirect:/admin_booking"; // Перенаправление на страницу admin_booking
                    } else {
                        return "redirect:/change_salon_page"; // Перенаправление на страницу создания салона
                    }
                } else {
                    session.setAttribute("userId", user.getId());
                    return "redirect:/home";
                }
            }
        }
        model.addAttribute("error", "Неверный email или пароль");
        return "login";
    }

    @Autowired
    private SalonService salonService;

    @GetMapping("/home")
    public String showHomePage(Model model) {
        List<Salon> salons = salonService.getAllSalons();
        model.addAttribute("salons", salons);
        return "home";
    }
    @GetMapping("/about_us")
    public String showAboutUsPage() {
        return "about_us";
    }

    @GetMapping("/contacts")
    public String showContactsPage() {
        return "contacts";
    }

}
