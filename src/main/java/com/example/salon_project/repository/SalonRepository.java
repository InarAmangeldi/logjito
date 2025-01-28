package com.example.salon_project.repository;

import com.example.salon_project.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalonRepository extends JpaRepository<Salon, Long> {
    boolean existsByAdminId(Long adminId);

    List<Salon> findByNameContainingIgnoreCase(String query);
    // Поиск салона по adminId
    Optional<Salon> findByAdminId(Long adminId);
}
