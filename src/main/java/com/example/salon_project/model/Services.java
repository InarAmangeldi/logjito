package com.example.salon_project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false) // Устанавливаем связь с Category
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration")
    private String duration;

    @Column(name = "price")
    private Double price;

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
