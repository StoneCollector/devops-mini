package com.cbms.controller;

import com.cbms.model.CateringSlot;
import com.cbms.model.Role;
import com.cbms.model.User;
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
public class SlotController {

    private final BookingService bookingService;
    private final UserService userService;

    public SlotController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/slots")
    public String listSlots(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            @RequestParam(required = false) String menuType,
                            HttpSession session,
                            Model model) {
        List<CateringSlot> slots = bookingService.listAvailableSlots(date, menuType);
        model.addAttribute("slots", slots);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedMenuType", menuType);
        return "slots";
    }

    @GetMapping("/vendor/slots")
    public String vendorSlots(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        List<CateringSlot> slots = bookingService.getSlotsByVendor(vendorOpt.get());
        model.addAttribute("slots", slots);
        return "vendor-slots";
    }

    @GetMapping("/vendor/slots/new")
    public String showCreateSlotForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        model.addAttribute("slot", new CateringSlot(LocalDate.now().plusDays(1), "Lunch (12:00 PM - 2:00 PM)", "", 20));
        model.addAttribute("isEdit", false);
        return "vendor-slot-form";
    }

    @PostMapping("/vendor/slots")
    public String createSlot(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String timeSlot,
                             @RequestParam String menuType,
                             @RequestParam Integer capacity,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        try {
            CateringSlot slot = new CateringSlot(date, timeSlot, menuType, capacity);
            bookingService.createSlot(vendorOpt.get(), slot);
            return "redirect:/vendor/slots?created=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("slot", new CateringSlot(date, timeSlot, menuType, capacity != null ? capacity : 0));
            model.addAttribute("isEdit", false);
            return "vendor-slot-form";
        }
    }

    @GetMapping("/vendor/slots/{id}/edit")
    public String showEditSlotForm(@PathVariable("id") Long slotId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<CateringSlot> slotOpt = bookingService.getSlotById(slotId);
        if (slotOpt.isEmpty() || slotOpt.get().getVendor() == null || !slotOpt.get().getVendor().getId().equals(userId)) {
            return "redirect:/vendor/slots";
        }

        model.addAttribute("slot", slotOpt.get());
        model.addAttribute("isEdit", true);
        return "vendor-slot-form";
    }

    @PostMapping("/vendor/slots/{id}")
    public String updateSlot(@PathVariable("id") Long slotId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String timeSlot,
                             @RequestParam String menuType,
                             @RequestParam Integer capacity,
                             HttpSession session,
                             Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        try {
            CateringSlot updatedSlot = new CateringSlot(date, timeSlot, menuType, capacity);
            bookingService.updateSlot(slotId, vendorOpt.get(), updatedSlot);
            return "redirect:/vendor/slots?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            CateringSlot slot = new CateringSlot(date, timeSlot, menuType, capacity != null ? capacity : 0);
            slot.setId(slotId);
            model.addAttribute("slot", slot);
            model.addAttribute("isEdit", true);
            return "vendor-slot-form";
        }
    }

    @PostMapping("/vendor/slots/{id}/delete")
    public String deleteSlot(@PathVariable("id") Long slotId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        try {
            bookingService.deleteSlot(slotId, vendorOpt.get());
            return "redirect:/vendor/slots?deleted=true";
        } catch (Exception e) {
            return "redirect:/vendor/slots?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}

