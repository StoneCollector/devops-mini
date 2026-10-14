package com.cbms.controller;

import com.cbms.model.CateringEvent;
import com.cbms.model.OccasionType;
import com.cbms.model.Role;
import com.cbms.model.ServiceStyle;
import com.cbms.model.ServiceType;
import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import com.cbms.model.VendorProfile;
import com.cbms.model.VendorResourcePool;
import com.cbms.repository.VendorCuisineRepository;
import com.cbms.repository.VendorResourcePoolRepository;
import com.cbms.service.BookingService;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class CateringEventController {

    private final BookingService bookingService;
    private final UserService userService;
    private final VendorCuisineRepository cuisineRepository;
    private final VendorResourcePoolRepository resourcePoolRepository;

    public CateringEventController(BookingService bookingService,
                                   UserService userService,
                                   VendorCuisineRepository cuisineRepository,
                                   VendorResourcePoolRepository resourcePoolRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.cuisineRepository = cuisineRepository;
        this.resourcePoolRepository = resourcePoolRepository;
    }

    @GetMapping("/events/book")
    public String showEventBookingForm(@RequestParam(required = false) Long vendorId,
                                       @RequestParam(required = false) Long cuisineId,
                                       HttpSession session,
                                       Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User customer = userOpt.get();
        model.addAttribute("customer", customer);

        // Fetch vendors
        List<User> vendors = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.VENDOR && u.isActive())
                .toList();
        model.addAttribute("vendors", vendors);

        Long selectedVendorId = vendorId != null ? vendorId : (vendors.isEmpty() ? null : vendors.get(0).getId());
        model.addAttribute("selectedVendorId", selectedVendorId);

        List<VendorCuisine> availableCuisines = (selectedVendorId != null)
                ? cuisineRepository.findByVendorIdOrderByCuisineNameAsc(selectedVendorId)
                : List.of();
        model.addAttribute("cuisines", availableCuisines);
        model.addAttribute("selectedCuisineId", cuisineId);

        // Fetch vendor resource pool & profile for context
        if (selectedVendorId != null) {
            Optional<VendorProfile> profileOpt = userService.getVendorProfile(selectedVendorId);
            profileOpt.ifPresent(p -> model.addAttribute("vendorProfile", p));

            Optional<VendorResourcePool> poolOpt = resourcePoolRepository.findByVendorId(selectedVendorId);
            poolOpt.ifPresent(pool -> model.addAttribute("resourcePool", pool));
        }

        model.addAttribute("occasionTypes", OccasionType.values());
        model.addAttribute("serviceTypes", ServiceType.values());
        model.addAttribute("serviceStyles", ServiceStyle.values());
        model.addAttribute("minDate", LocalDate.now().plusDays(1).toString());

        return "event-booking-form";
    }

    @PostMapping("/events/book")
    public String bookEvent(@RequestParam Long vendorId,
                            @RequestParam(required = false) Long cuisineId,
                            @RequestParam String occasionType,
                            @RequestParam String serviceType,
                            @RequestParam(required = false) String serviceStyle,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                            @RequestParam(required = false) String eventTimeFrom,
                            @RequestParam(required = false) String eventTimeTo,
                            @RequestParam Integer guestCount,
                            @RequestParam(required = false) String specialNotes,
                            @RequestParam(required = false) String deliveryAddress,
                            @RequestParam(required = false) String pickupTime,
                            HttpSession session,
                            Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            OccasionType occasion = OccasionType.valueOf(occasionType);
            ServiceType sType = ServiceType.valueOf(serviceType);
            ServiceStyle style = (serviceStyle != null && !serviceStyle.isBlank())
                    ? ServiceStyle.valueOf(serviceStyle) : ServiceStyle.BUFFET;

            bookingService.createEventBooking(
                    userId, vendorId, cuisineId, occasion, sType, style,
                    eventDate, eventTimeFrom, eventTimeTo,
                    guestCount != null ? guestCount : 1,
                    specialNotes, deliveryAddress, pickupTime
            );

            return "redirect:/bookings/me?eventSuccess=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());

            Optional<User> userOpt = userService.findById(userId);
            userOpt.ifPresent(u -> model.addAttribute("customer", u));

            List<User> vendors = userService.getAllUsers().stream()
                    .filter(u -> u.getRole() == Role.VENDOR && u.isActive())
                    .toList();
            model.addAttribute("vendors", vendors);
            model.addAttribute("selectedVendorId", vendorId);
            model.addAttribute("selectedCuisineId", cuisineId);

            List<VendorCuisine> availableCuisines = cuisineRepository.findByVendorIdOrderByCuisineNameAsc(vendorId);
            model.addAttribute("cuisines", availableCuisines);

            model.addAttribute("occasionTypes", OccasionType.values());
            model.addAttribute("serviceTypes", ServiceType.values());
            model.addAttribute("serviceStyles", ServiceStyle.values());
            model.addAttribute("minDate", LocalDate.now().plusDays(1).toString());

            return "event-booking-form";
        }
    }

    @GetMapping("/events/{id}")
    public String viewEventDetail(@PathVariable("id") Long eventId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<CateringEvent> eventOpt = bookingService.getEventById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/bookings/me";
        }

        CateringEvent event = eventOpt.get();
        boolean isAuthorized = event.getCustomer().getId().equals(userId)
                || event.getVendor().getId().equals(userId)
                || "ADMIN".equals(role);

        if (!isAuthorized) {
            return "redirect:/slots";
        }

        model.addAttribute("event", event);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("currentUserRole", role);

        // Load vendor profile and resource usage on event date
        Optional<VendorProfile> profileOpt = userService.getVendorProfile(event.getVendor().getId());
        profileOpt.ifPresent(p -> model.addAttribute("vendorProfile", p));

        BookingService.DailyResourceUsage dailyUsage = bookingService.getResourceUsageOnDate(event.getVendor().getId(), event.getEventDate());
        model.addAttribute("dailyUsage", dailyUsage);

        return "event-detail";
    }

    @PostMapping("/events/{id}/confirm")
    public String vendorConfirmEvent(@PathVariable("id") Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isPresent()) {
            try {
                bookingService.confirmEventByVendor(eventId, vendorOpt.get());
                return "redirect:/vendor/bookings?confirmed=true";
            } catch (Exception e) {
                return "redirect:/vendor/bookings?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/vendor/bookings";
    }

    @PostMapping("/events/{id}/reject")
    public String vendorRejectEvent(@PathVariable("id") Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isPresent()) {
            try {
                bookingService.rejectEventByVendor(eventId, vendorOpt.get());
                return "redirect:/vendor/bookings?rejected=true";
            } catch (Exception e) {
                return "redirect:/vendor/bookings?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/vendor/bookings";
    }

    @PostMapping("/events/{id}/cancel")
    public String customerCancelEvent(@PathVariable("id") Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            try {
                bookingService.cancelEventByCustomer(eventId, userOpt.get());
                return "redirect:/bookings/me?cancelled=true";
            } catch (Exception e) {
                return "redirect:/bookings/me?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "redirect:/bookings/me";
    }
}
