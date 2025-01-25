package com.example.salon_project.request;

import java.util.List;

public class CategoryRequest {
    private String name;
    private List<ServiceRequest> services;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceRequest> getServices() {
        return services;
    }

    public void setServices(List<ServiceRequest> services) {
        this.services = services;
    }
}
