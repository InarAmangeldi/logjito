package com.example.salon_project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "salon_id", nullable = false) // Устанавливаем связь с Salon
    private Salon salon;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Services> services;

    @Column(name = "name", nullable = false)
    private String name;

    // Getters и Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Salon getSalon() { return salon; }
    public void setSalon(Salon salon) { this.salon = salon; }

    public List<Services> getServices() { return services; }
    public void setServices(List<Services> services) { this.services = services; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
