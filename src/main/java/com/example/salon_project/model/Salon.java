package com.example.salon_project.model;

import com.example.salon_project.request.CategoryRequest;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salon_info")
public class Salon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String info;

    @Column(name = "admin_id")
    private Long adminId;

    @Transient // Это временно, так как данные категории не сохраняются в этой таблице
    private List<CategoryRequest> categories;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos;


    // Getters и Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public List<CategoryRequest> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryRequest> categories) {
        this.categories = categories;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
