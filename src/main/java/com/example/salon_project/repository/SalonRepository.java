package com.example.salon_project.repository;

import com.example.salon_project.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalonRepository extends JpaRepository<Salon, Long> {
    boolean existsByAdminId(Long adminId);

    // Поиск салона по adminId
    Optional<Salon> findByAdminId(Long adminId);
}
