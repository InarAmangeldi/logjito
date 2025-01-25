package com.example.salon_project.service;

import com.example.salon_project.model.Category;
import com.example.salon_project.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> findBySalonId(Long salonId) {
        return categoryRepository.findBySalonId(salonId);
    }

}

