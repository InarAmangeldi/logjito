package com.example.salon_project.controller;

import com.example.salon_project.model.Photo;
import com.example.salon_project.model.Salon;
import com.example.salon_project.service.PhotoService;
import com.example.salon_project.service.SalonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private SalonService salonService;

    @PostMapping("/upload-photo")
    public ResponseEntity<String> uploadPhoto(
            @RequestParam("salonId") Long salonId,
            @RequestParam("photos") MultipartFile[] photos) {
        for (MultipartFile photo : photos) {
            try {
                // Генерация уникального имени файла
                String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();

                // Определяем путь для сохранения
                Path uploadPath = Paths.get(System.getProperty("user.dir") + "/uploads/");
                Files.createDirectories(uploadPath); // Создаем папку, если её нет

                Path filePath = uploadPath.resolve(fileName); // Полный путь файла
                Files.write(filePath, photo.getBytes()); // Сохраняем файл на диск

                // Сохраняем информацию о фото в базу данных
                Photo photoEntity = new Photo();
                Salon salon = salonService.findById(salonId); // Получаем объект Salon по ID
                photoEntity.setSalon(salon);
                photoEntity.setPhotoUrl("/uploads/" + fileName); // Относительный путь
                photoService.save(photoEntity);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при загрузке файла: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Фотографии успешно сохранены");
    }

}
