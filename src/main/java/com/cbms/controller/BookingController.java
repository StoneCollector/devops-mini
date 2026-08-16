package com.cbms.controller;

import com.cbms.model.User;
import com.cbms.service.BookingService;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("slots", bookingService.getUpcomingSlots());
        return "index";
    }

    @PostMapping("/book")
    public String bookSlot(@RequestParam Long slotId, 
                           @RequestParam int guests, 
                           HttpSession session, 
                           Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            try {
                bookingService.createBooking(userOpt.get(), slotId, guests);
                return "redirect:/my-bookings?success=true";
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("slots", bookingService.getUpcomingSlots());
                return "index";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            model.addAttribute("bookings", bookingService.getUserBookings(userOpt.get()));
            return "my-bookings";
        }
        return "redirect:/login";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam Long bookingId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            try {
                bookingService.cancelBooking(bookingId, userOpt.get());
            } catch (Exception e) {
                // Ignore for simple MVP or redirect with error
            }
        }
        return "redirect:/my-bookings";
    }
}
