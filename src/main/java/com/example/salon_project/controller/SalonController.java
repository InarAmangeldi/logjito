package com.example.salon_project.controller;

import com.example.salon_project.model.*;
import com.example.salon_project.request.CategoryRequest;
import com.example.salon_project.request.ServiceRequest;
import com.example.salon_project.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class SalonController {

    @Autowired
    private SalonService salonService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    // Переход на страницу изменения салона
    @GetMapping("/change_salon_page")
    public String showChangeSalonPage(HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/login"; // Перенаправление на логин, если админ не авторизован
        }
        return "change_salon_page"; // Возвращаем имя HTML-шаблона
    }

    @PostMapping(value = "/save", consumes = "multipart/form-data")
    public ResponseEntity<String> saveSalonWithDetails(
            @RequestParam("name") String name,
            @RequestParam("info") String info,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam Map<String, String> params,
            HttpSession session
    ) {
        try {
            Long adminId = (Long) session.getAttribute("adminId");
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ошибка: Вы не авторизованы как администратор.");
            }

            if (salonService.existsByAdminId(adminId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Ошибка: Вы уже создали салон. Пожалуйста, отредактируйте существующий салон.");
            }

            // Создаем новый объект салона
            Salon salon = new Salon();
            salon.setName(name);
            salon.setInfo(info);
            salon.setAdminId(adminId);

            // Сохраняем фото
            if (!photo.isEmpty()) {
                String uploadDir = "src/main/resources/static/uploads/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, photo.getBytes());

                salon.setPhotoUrl("/uploads/" + fileName);
            }

            // Сохраняем салон в базу
            Salon savedSalon = salonService.saveSalon(salon);

            // Обрабатываем категории
            Map<Integer, Category> categoryMap = new HashMap<>();

            for (String key : params.keySet()) {
                if (key.matches("categories\\[\\d+\\]\\.name")) {
                    int categoryIndex = Integer.parseInt(key.replaceAll("\\D", ""));
                    Category category = new Category();
                    category.setName(params.get(key));
                    category.setSalon(savedSalon); // Устанавливаем связь с салоном
                    category = categoryService.saveCategory(category); // Сохраняем и обновляем объект
                    categoryMap.put(categoryIndex, category);
                }
            }

            // Обрабатываем услуги
            for (String key : params.keySet()) {
                if (key.matches("categories\\[\\d+\\]\\.services\\[\\d+\\]\\.name")) {
                    // Извлекаем индексы категории и услуги
                    Matcher matcher = Pattern.compile("categories\\[(\\d+)]\\.services\\[(\\d+)]\\.name").matcher(key);
                    if (matcher.find()) {
                        int categoryIndex = Integer.parseInt(matcher.group(1));
                        int serviceIndex = Integer.parseInt(matcher.group(2));

                        Category category = categoryMap.get(categoryIndex);
                        if (category != null) {
                            Services service = new Services();
                            service.setName(params.get(key));
                            service.setCategory(category); // Привязываем к категории

                            // Устанавливаем дополнительные параметры услуги
                            String durationKey = key.replace(".name", ".duration");
                            String priceKey = key.replace(".name", ".price");

                            if (params.containsKey(durationKey)) {
                                service.setDuration(params.get(durationKey));
                            }
                            if (params.containsKey(priceKey)) {
                                try {
                                    service.setPrice(Double.parseDouble(params.get(priceKey)));
                                } catch (NumberFormatException e) {
                                    System.out.println("Ошибка парсинга цены: " + params.get(priceKey));
                                }
                            }

                            // Сохраняем услугу
                            servicesService.saveService(service);
                        }
                    }
                }
            }

            return ResponseEntity.ok("Данные успешно сохранены!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при сохранении данных: " + e.getMessage());
        }
    }






    @GetMapping("/salon/{id}")
    public String getSalonById(@PathVariable("id") Long id, Model model) {
        Salon salon = salonService.findById(id); // Получаем салон по ID

        // Получаем категории и услуги
        List<Category> categories = categoryService.findBySalonId(salon.getId());
        for (Category category : categories) {
            List<Services> services = servicesService.findByCategoryId(category.getId());
            category.setServices(services);
        }
        salon.setCategories(categories);

        model.addAttribute("salon", salon);
        return "salon_page";
    }

    @PostMapping("/saveBooking")
    public String saveBooking(
            @RequestParam Long salonId,
            @RequestParam String clientName,
            @RequestParam Long serviceId,
            @RequestParam String date,
            @RequestParam String time,
            HttpSession session,
            Model model) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // Перенаправление на логин, если пользователь не авторизован
        }

        User user = userService.findById(userId);

        // Логика сохранения данных в базу
        Booking booking = new Booking();
        booking.setSalonId(salonId);
        booking.setClientName(clientName);
        booking.setServiceId(serviceId);
        booking.setDate(date);
        booking.setTime(time);
        booking.setUser(user);

        bookingService.save(booking);

        model.addAttribute("message", "Бронирование успешно сохранено!");
        return "redirect:/salon/" + salonId;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; // Перенаправление на страницу логина
        }

        // Получаем бронирования пользователя
        List<Booking> bookings = bookingService.findByUserId(userId);

        // Дополняем информацию о салоне и услуге
        for (Booking booking : bookings) {
            Salon salon = salonService.findById(booking.getSalonId());
            if (salon != null) {
                booking.setSalonName(salon.getName());
            }

            Services service = servicesService.findById(booking.getServiceId());
            if (service != null) {
                booking.setServiceName(service.getName()); // Заполняем название услуги
                booking.setPrice(service.getPrice()); // Заполняем цену услуги
            }
        }



        // Добавляем бронирования в модель
        model.addAttribute("bookings", bookings);

        return "profile"; // Возвращаем шаблон профиля
    }


    @GetMapping("/admin_booking")
    public String showAdminBookings(HttpSession session, Model model,
                                    @RequestParam(value = "selectedDate", required = false) String selectedDate) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/login";
        }

        // Получение ID салона, связанного с администратором
        Salon salon = salonService.findByAdminId(adminId);
        if (salon == null) {
            return "redirect:/change_salon_page"; // Если салон не найден
        }

        // Выборка заказов по дате
        List<Booking> bookings;
        if (selectedDate != null) {
            bookings = bookingService.findBookingsBySalonAndDate(salon.getId(), selectedDate);
        } else {
            bookings = bookingService.findBookingsBySalon(salon.getId());
        }

        // Заполнение поля serviceName
        for (Booking booking : bookings) {
            Services service = servicesService.findById(booking.getServiceId());
            if (service != null) {
                booking.setServiceName(service.getName());
            }
        }

        model.addAttribute("currentDate", selectedDate);
        model.addAttribute("bookings", bookings);
        return "admin_booking";
    }

    @GetMapping("/search")
    public String searchSalons(@RequestParam("query") String query, Model model) {
        List<Salon> foundSalons = salonService.findByNameContainingIgnoreCase(query);
        model.addAttribute("salons", foundSalons);
        return "home"; // Возвращает ту же страницу с обновленным списком
    }

    @PostMapping("/deleteBookings")
    @ResponseBody
    public ResponseEntity<String> deleteBookings(@RequestBody List<Long> bookingIds) {
        bookingService.deleteBookingsByIds(bookingIds);
        return ResponseEntity.ok("Удалено успешно");
    }



}
