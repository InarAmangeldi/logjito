package com.example.salon_project.repository;

import com.example.salon_project.model.Photo;
import com.example.salon_project.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findBySalon(Salon salon);  // Вместо findBySalonId используем объект Salon
}
