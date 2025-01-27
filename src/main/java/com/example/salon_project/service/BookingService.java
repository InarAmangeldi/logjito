package com.example.salon_project.service;

import com.example.salon_project.model.Booking;
import com.example.salon_project.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    public void save(Booking booking) {
        bookingRepository.save(booking);
    }

    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> findBookingsBySalon(Long salonId) {
        return bookingRepository.findBySalonId(salonId);
    }

    public List<Booking> findBookingsBySalonAndDate(Long salonId, String date) {
        return bookingRepository.findBySalonIdAndDate(salonId, date);
    }


}
