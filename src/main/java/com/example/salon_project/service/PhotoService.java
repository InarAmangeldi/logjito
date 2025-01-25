package com.example.salon_project.service;

import com.example.salon_project.model.Photo;
import com.example.salon_project.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    public void save(Photo photo) {
        photoRepository.save(photo);
    }
}

