package com.example.salon_project.repository;

import com.example.salon_project.model.Booking;
import com.example.salon_project.model.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Services, Long> {
    List<Services> findByCategoryId(Long categoryId);


}

