package com.example.salon_project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "salon_photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false)
//    private Long salonId;

    @Column(nullable = false)
    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "salon_id", referencedColumnName = "id") // Используйте одно имя
    private Salon salon;

    // Getters и Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }
}
