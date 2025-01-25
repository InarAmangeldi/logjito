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
                        Model model,
                        HttpSession session
    ) {
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(password)) {
                if ("ADMIN".equals(user.getRole())) {
                    session.setAttribute("adminId", user.getId());
                    return "redirect:/change_salon_page";
                } else {
                    session.setAttribute("userId", user.getId());
                    return "redirect:/home";
                }
            }
        }
        model.addAttribute("error", "Неверный email или пароль");
        return "login";
    }


    @GetMapping("/admin_main")
    public String showAdminMainPage(Model model) {
        // Если необходимо передать данные для страницы, сделайте это здесь
        return "admin_main"; // Имя HTML-шаблона
    }


    @Autowired
    private SalonService salonService;

    @GetMapping("/home")
    public String showHomePage(Model model) {
        List<Salon> salons = salonService.getAllSalons();
        model.addAttribute("salons", salons);
        return "home";
    }

//    @GetMapping("/salon/{id}")
//    public String getSalonById(@PathVariable("id") Long id, Model model) {
//        Salon salon = salonService.findById(id); // Метод для поиска салона по ID
//        model.addAttribute("salon", salon);
//        model.addAttribute("mainPhoto", salon.getPhotos().isEmpty() ? "/uploads/placeholder.jpg" : salon.getPhotos().get(0).getPhotoUrl()); // Берем первое фото
//        return "salon_page"; // Название HTML-шаблона
//    }






}
