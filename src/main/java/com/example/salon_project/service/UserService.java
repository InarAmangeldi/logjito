package com.example.salon_project.service;

import com.example.salon_project.model.User;
import com.example.salon_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public List<User> getAllAdmins() {
        return userRepository.findByRole("ADMIN");
    }





}
