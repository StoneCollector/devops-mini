package com.cbms.controller;

import com.cbms.model.Booking;
import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.service.BookingService;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
    public String index(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("userRole");
            if ("VENDOR".equals(role)) return "redirect:/vendor/slots";
            if ("ADMIN".equals(role)) return "redirect:/admin";
            return "redirect:/slots";
        }
        return "redirect:/login";
    }

    @PostMapping({"/bookings", "/book"})
    public String createBooking(@RequestParam Long slotId,
                                @RequestParam(name = "numberOfGuests", required = false) Integer numberOfGuests,
                                @RequestParam(name = "guests", required = false) Integer guests,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        int numGuests = (numberOfGuests != null) ? numberOfGuests : (guests != null ? guests : 1);

        try {
            bookingService.createBooking(userId, slotId, numGuests);
            return "redirect:/bookings/me?success=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("slots", bookingService.listAvailableSlots(null));
            return "slots";
        }
    }

    @GetMapping({"/bookings/me", "/my-bookings"})
    public String myBookings(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.listBookingsForUser(userId);
        model.addAttribute("bookings", bookings);
        return "my-bookings";
    }

    @RequestMapping(value = {"/bookings/{id}/cancel", "/cancel-booking"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public String cancelBooking(@PathVariable(value = "id", required = false) Long pathId,
                                @RequestParam(value = "bookingId", required = false) Long paramId,
                                HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Long bookingId = (pathId != null) ? pathId : paramId;
        if (bookingId != null) {
            try {
                bookingService.cancelBooking(bookingId, userId);
            } catch (Exception e) {
                // Handled gracefully
            }
        }
        return "redirect:/bookings/me?cancelled=true";
    }

    // Vendor: View bookings made against their slots
    @GetMapping({"/vendor/bookings", "/bookings/vendor"})
    public String vendorBookings(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.getBookingsForVendor(vendorOpt.get());
        model.addAttribute("bookings", bookings);
        return "vendor-bookings";
    }

    // Vendor: Confirm booking on their slot
    @PostMapping("/vendor/bookings/{id}/confirm")
    public String vendorConfirmBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isPresent()) {
            try {
                bookingService.confirmBookingByVendor(bookingId, vendorOpt.get());
            } catch (Exception e) {
                return "redirect:/vendor/bookings?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/vendor/bookings?confirmed=true";
    }

    // Vendor: Reject booking on their slot
    @PostMapping("/vendor/bookings/{id}/reject")
    public String vendorRejectBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isPresent()) {
            try {
                bookingService.rejectBookingByVendor(bookingId, vendorOpt.get());
            } catch (Exception e) {
                return "redirect:/vendor/bookings?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/vendor/bookings?rejected=true";
    }

    // Legacy/Admin direct confirm handler (backward compatibility)
    @RequestMapping(value = "/bookings/{id}/confirm", method = {RequestMethod.PUT, RequestMethod.POST})
    public String legacyConfirmBooking(@PathVariable("id") Long bookingId, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/slots";
        }

        try {
            bookingService.confirmBooking(bookingId);
        } catch (Exception e) {
            // Handled gracefully
        }
        return "redirect:/admin/bookings";
    }

    // Admin: List all bookings system-wide (read-only)
    @GetMapping({"/bookings", "/admin/bookings"})
    public String listAllBookings(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            return "redirect:/slots";
        }

        List<Booking> bookings = bookingService.listAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin-bookings";
    }
}

