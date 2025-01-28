package com.example.salon_project.service;

import com.example.salon_project.model.Salon;
import com.example.salon_project.repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalonService {

    @Autowired
    private SalonRepository salonRepository;

    public Salon saveSalon(Salon salon) {
        return salonRepository.save(salon);
    }

    // Проверка существования салона для adminId
    public boolean existsByAdminId(Long adminId) {
        return salonRepository.existsByAdminId(adminId);
    }

    // Получение салона по adminId
    public Salon findByAdminId(Long adminId) {
        return salonRepository.findByAdminId(adminId).orElse(null);
    }

    public List<Salon> getAllSalons() {
        return salonRepository.findAll();
    }

    public Salon findById(Long salonId) {
        return salonRepository.findById(salonId)
                .orElseThrow(() -> new IllegalArgumentException("Salon not found with id: " + salonId));
    }


    public List<Salon> findByNameContainingIgnoreCase(String query) {
        return salonRepository.findByNameContainingIgnoreCase(query);
    }

}
