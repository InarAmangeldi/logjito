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
            @RequestParam("photo") MultipartFile photo) {
        if (photo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Фото не было загружено");
        }

        try {
            // Проверка типа файла
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Только изображения можно загружать");
            }

            // Генерация уникального имени файла
            String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();

            // Определяем путь для сохранения
            Path uploadPath = Paths.get(System.getProperty("user.dir") + "/uploads/");
            Files.createDirectories(uploadPath); // Создаем папку, если её нет

            Path filePath = uploadPath.resolve(fileName); // Полный путь файла

            // Проверка на существование файла с таким же именем
            if (Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Файл с таким именем уже существует");
            }

            // Сохраняем файл на диск
            Files.write(filePath, photo.getBytes());

            // Сохраняем информацию о фото в базе данных
            Photo photoEntity = new Photo();
            Salon salon = salonService.findById(salonId); // Получаем объект Salon по ID
            if (salon == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Салон с таким ID не найден");
            }
            photoEntity.setSalon(salon);
            photoEntity.setPhotoUrl("/uploads/" + fileName); // Относительный путь
            photoService.save(photoEntity);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при загрузке файла: " + e.getMessage());
        }

        return ResponseEntity.ok("Фотография успешно сохранена");
    }
}
