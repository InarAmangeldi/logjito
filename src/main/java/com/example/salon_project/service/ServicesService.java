package com.example.salon_project.service;

import com.example.salon_project.model.Services;
import com.example.salon_project.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicesService { // Переименовали класс
    @Autowired
    private ServiceRepository serviceRepository;

    public Services saveService(Services service) {
        return serviceRepository.save(service);
    }
}
