package com.cbms.controller;

import com.cbms.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final BookingService bookingService;

    public AdminController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/";
        }

        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin-dashboard";
    }

    @PostMapping("/admin/confirm-booking")
    public String confirmBooking(@RequestParam Long bookingId, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/";
        }

        try {
            bookingService.confirmBooking(bookingId);
        } catch (Exception e) {
            // Error handling ignored for MVP simplicity
        }
        
        return "redirect:/admin";
    }
}
