package com.cbms.controller;

import com.cbms.model.Booking;
import com.cbms.model.User;
import com.cbms.service.BookingService;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class AdminController {

    private final BookingService bookingService;
    private final UserService userService;

    public AdminController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping({"/admin", "/admin/users"})
    public String adminUsers(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/slots";
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long targetUserId, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        Long currentAdminId = (Long) session.getAttribute("userId");

        if (!"ADMIN".equals(role)) {
            return "redirect:/slots";
        }

        // Prevent admin from deactivating their own account
        if (targetUserId.equals(currentAdminId)) {
            return "redirect:/admin/users?error=Cannot+deactivate+your+own+admin+account";
        }

        try {
            userService.toggleUserActive(targetUserId);
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }

        return "redirect:/admin/users?updated=true";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/slots";
        }
        return "redirect:/admin/users";
    }
}

