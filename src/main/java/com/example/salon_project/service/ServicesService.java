package com.example.salon_project.service;

import com.example.salon_project.model.Services;
import com.example.salon_project.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicesService { // Переименовали класс
    @Autowired
    private ServiceRepository serviceRepository;

    public Services saveService(Services service) {
        return serviceRepository.save(service);
    }

    public List<Services> findByCategoryId(Long categoryId) {
        return serviceRepository.findByCategoryId(categoryId);
    }

    public Services findById(Long id) {
        return serviceRepository.findById(id).orElse(null);
    }


}
