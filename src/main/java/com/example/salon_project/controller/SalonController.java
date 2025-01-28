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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class SalonController {

    @Autowired
    private SalonService salonService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ServicesService servicesService;

    @Autowired
    private PhotoService photoService;

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
            @RequestParam("photos") MultipartFile[] photos,
            @RequestParam Map<String, String> categories,
            HttpSession session
    ) {
        try {
            Long adminId = (Long) session.getAttribute("adminId");
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ошибка: Вы не авторизованы как администратор.");
            }

            // Проверяем, существует ли уже салон для данного adminId
            if (salonService.existsByAdminId(adminId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Ошибка: Вы уже создали салон. Пожалуйста, отредактируйте существующий салон.");
            }

            // Сохраняем салон
            Salon salon = new Salon();
            salon.setName(name);
            salon.setInfo(info);
            salon.setAdminId(adminId);
            Salon savedSalon = salonService.saveSalon(salon);

            // Обработка категорий и услуг
            Map<Integer, Category> savedCategories = new HashMap<>();
            Map<Integer, Map<Integer, Services>> servicesBuffer = new HashMap<>();

            categories.forEach((key, value) -> {
                if (key.matches("categories\\[\\d+\\]\\.name")) {
                    // Обрабатываем категорию
                    Integer categoryIndex = Integer.parseInt(key.replaceAll("\\D+", ""));
                    Category category = new Category();
                    category.setName(value);
                    category.setSalonId(savedSalon.getId());
                    System.out.println("Saving category with salonId: " + savedSalon.getId());
                    savedCategories.put(categoryIndex, categoryService.saveCategory(category));
                } else if (key.matches("categories\\[\\d+\\]\\.services\\[\\d+\\]\\..+")) {
                    // Обрабатываем услугу
                    String[] keys = key.split("\\.");
                    Integer categoryIndex = Integer.parseInt(keys[0].replaceAll("\\D+", ""));
                    Integer serviceIndex = Integer.parseInt(keys[1].replaceAll("\\D+", ""));
                    String serviceField = keys[2];

                    // Инициализируем буфер для услуг, если он отсутствует
                    servicesBuffer.putIfAbsent(categoryIndex, new HashMap<>());
                    Map<Integer, Services> categoryServices = servicesBuffer.get(categoryIndex);
                    categoryServices.putIfAbsent(serviceIndex, new Services());

                    Services service = categoryServices.get(serviceIndex);

                    // Устанавливаем поля услуги
                    switch (serviceField) {
                        case "name" -> service.setName(value);
                        case "duration" -> service.setDuration(value);
                        case "price" -> service.setPrice(Double.parseDouble(value));
                    }
                }
            });

            // Сохраняем услуги после обработки всех данных
            servicesBuffer.forEach((categoryIndex, servicesMap) -> {
                Category parentCategory = savedCategories.get(categoryIndex);
                if (parentCategory != null) {
                    servicesMap.forEach((serviceIndex, service) -> {
                        if (service.getName() != null && service.getDuration() != null && service.getPrice() != null) {
                            service.setCategoryId(parentCategory.getId());
                            servicesService.saveService(service);
                        }
                    });
                }
            });


            // Сохраняем фотографии
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads/" + fileName);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, photo.getBytes());

                    Photo photoEntity = new Photo();
                    photoEntity.setSalon(savedSalon);
                    photoEntity.setPhotoUrl("/uploads/" + fileName);
                    photoService.save(photoEntity);
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

        // Дополняем информацию о салоне
        for (Booking booking : bookings) {
            Salon salon = salonService.findById(booking.getSalonId());
            if (salon != null) {
                booking.setSalonName(salon.getName());
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








}
