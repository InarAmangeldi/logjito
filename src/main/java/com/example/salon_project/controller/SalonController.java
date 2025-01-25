package com.example.salon_project.controller;

import com.example.salon_project.model.Category;
import com.example.salon_project.model.Photo;
import com.example.salon_project.model.Salon;
import com.example.salon_project.model.Services;
import com.example.salon_project.request.CategoryRequest;
import com.example.salon_project.request.ServiceRequest;
import com.example.salon_project.service.CategoryService;
import com.example.salon_project.service.PhotoService;
import com.example.salon_project.service.SalonService;
import com.example.salon_project.service.ServicesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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


}
