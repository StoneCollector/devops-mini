package com.cbms.controller;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.model.VendorResourcePool;
import com.cbms.repository.VendorResourcePoolRepository;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/vendor/resources")
public class VendorResourceController {

    private final VendorResourcePoolRepository resourcePoolRepository;
    private final UserService userService;

    public VendorResourceController(VendorResourcePoolRepository resourcePoolRepository, UserService userService) {
        this.resourcePoolRepository = resourcePoolRepository;
        this.userService = userService;
    }

    @GetMapping
    public String viewResources(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        User vendor = vendorOpt.get();
        VendorResourcePool pool = resourcePoolRepository.findByVendor(vendor)
                .orElseGet(() -> {
                    VendorResourcePool newPool = new VendorResourcePool(vendor, 12, 150, 3, 100);
                    return resourcePoolRepository.save(newPool);
                });

        model.addAttribute("pool", pool);
        return "vendor-resources";
    }

    @PostMapping
    public String updateResources(@RequestParam(defaultValue = "0") int staffCount,
                                  @RequestParam(defaultValue = "0") int utensilSets,
                                  @RequestParam(defaultValue = "0") int deliveryVehicles,
                                  @RequestParam(defaultValue = "0") int venueCoverCapacity,
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

        User vendor = vendorOpt.get();
        VendorResourcePool pool = resourcePoolRepository.findByVendor(vendor)
                .orElseGet(() -> new VendorResourcePool(vendor, 12, 150, 3, 100));

        pool.setStaffCount(staffCount);
        pool.setUtensilSets(utensilSets);
        pool.setDeliveryVehicles(deliveryVehicles);
        pool.setVenueCoverCapacity(venueCoverCapacity);

        resourcePoolRepository.save(pool);

        return "redirect:/vendor/resources?saved=true";
    }
}
