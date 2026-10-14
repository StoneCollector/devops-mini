package com.cbms.controller;

import com.cbms.model.DietaryFlag;
import com.cbms.model.ServiceStyle;
import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import com.cbms.repository.VendorCuisineRepository;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/cuisines")
public class VendorCuisineController {

    private final VendorCuisineRepository cuisineRepository;
    private final UserService userService;

    public VendorCuisineController(VendorCuisineRepository cuisineRepository, UserService userService) {
        this.cuisineRepository = cuisineRepository;
        this.userService = userService;
    }

    @GetMapping
    public String listCuisines(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<User> vendorOpt = userService.findById(userId);
        if (vendorOpt.isEmpty()) {
            return "redirect:/login";
        }

        List<VendorCuisine> cuisines = cuisineRepository.findByVendorOrderByCuisineNameAsc(vendorOpt.get());
        model.addAttribute("cuisines", cuisines);
        return "vendor-cuisines";
    }

    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        model.addAttribute("cuisine", new VendorCuisine());
        model.addAttribute("allStyles", ServiceStyle.values());
        model.addAttribute("allDietary", DietaryFlag.values());
        model.addAttribute("isEdit", false);
        return "vendor-cuisine-form";
    }

    @PostMapping
    public String createCuisine(@RequestParam String cuisineName,
                                @RequestParam(required = false) List<String> serviceStyles,
                                @RequestParam(required = false) Double pricePerHead,
                                @RequestParam(defaultValue = "15") Integer minGuests,
                                @RequestParam(defaultValue = "500") Integer maxGuests,
                                @RequestParam(required = false) List<String> dietaryFlags,
                                @RequestParam(required = false) String description,
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
            if (cuisineName == null || cuisineName.trim().isEmpty()) {
                throw new IllegalArgumentException("Cuisine name cannot be empty");
            }
            if (minGuests != null && maxGuests != null && minGuests > maxGuests) {
                throw new IllegalArgumentException("Minimum guests cannot exceed maximum guests");
            }

            String stylesStr = (serviceStyles != null && !serviceStyles.isEmpty()) ? String.join(",", serviceStyles) : "BUFFET";
            String dietaryStr = (dietaryFlags != null && !dietaryFlags.isEmpty()) ? String.join(",", dietaryFlags) : "";

            VendorCuisine cuisine = new VendorCuisine(
                    vendorOpt.get(),
                    cuisineName.trim(),
                    stylesStr,
                    pricePerHead,
                    minGuests != null ? minGuests : 15,
                    maxGuests != null ? maxGuests : 500,
                    dietaryStr,
                    description != null ? description.trim() : ""
            );
            cuisineRepository.save(cuisine);
            return "redirect:/vendor/cuisines?created=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cuisine", new VendorCuisine());
            model.addAttribute("allStyles", ServiceStyle.values());
            model.addAttribute("allDietary", DietaryFlag.values());
            model.addAttribute("isEdit", false);
            return "vendor-cuisine-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<VendorCuisine> cuisineOpt = cuisineRepository.findById(id);
        if (cuisineOpt.isEmpty() || !cuisineOpt.get().getVendor().getId().equals(userId)) {
            return "redirect:/vendor/cuisines";
        }

        model.addAttribute("cuisine", cuisineOpt.get());
        model.addAttribute("allStyles", ServiceStyle.values());
        model.addAttribute("allDietary", DietaryFlag.values());
        model.addAttribute("isEdit", true);
        return "vendor-cuisine-form";
    }

    @PostMapping("/{id}")
    public String updateCuisine(@PathVariable("id") Long id,
                                @RequestParam String cuisineName,
                                @RequestParam(required = false) List<String> serviceStyles,
                                @RequestParam(required = false) Double pricePerHead,
                                @RequestParam(defaultValue = "15") Integer minGuests,
                                @RequestParam(defaultValue = "500") Integer maxGuests,
                                @RequestParam(required = false) List<String> dietaryFlags,
                                @RequestParam(required = false) String description,
                                @RequestParam(defaultValue = "false") boolean active,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<VendorCuisine> cuisineOpt = cuisineRepository.findById(id);
        if (cuisineOpt.isEmpty() || !cuisineOpt.get().getVendor().getId().equals(userId)) {
            return "redirect:/vendor/cuisines";
        }

        try {
            if (cuisineName == null || cuisineName.trim().isEmpty()) {
                throw new IllegalArgumentException("Cuisine name cannot be empty");
            }
            if (minGuests != null && maxGuests != null && minGuests > maxGuests) {
                throw new IllegalArgumentException("Minimum guests cannot exceed maximum guests");
            }

            VendorCuisine cuisine = cuisineOpt.get();
            cuisine.setCuisineName(cuisineName.trim());
            cuisine.setServiceStyles((serviceStyles != null && !serviceStyles.isEmpty()) ? String.join(",", serviceStyles) : "BUFFET");
            cuisine.setDietaryFlags((dietaryFlags != null && !dietaryFlags.isEmpty()) ? String.join(",", dietaryFlags) : "");
            cuisine.setPricePerHead(pricePerHead);
            cuisine.setMinGuests(minGuests != null ? minGuests : 15);
            cuisine.setMaxGuests(maxGuests != null ? maxGuests : 500);
            cuisine.setDescription(description != null ? description.trim() : "");
            cuisine.setActive(active);

            cuisineRepository.save(cuisine);
            return "redirect:/vendor/cuisines?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cuisine", cuisineOpt.get());
            model.addAttribute("allStyles", ServiceStyle.values());
            model.addAttribute("allDietary", DietaryFlag.values());
            model.addAttribute("isEdit", true);
            return "vendor-cuisine-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCuisine(@PathVariable("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("userRole");

        if (userId == null || !"VENDOR".equals(role)) {
            return "redirect:/slots";
        }

        Optional<VendorCuisine> cuisineOpt = cuisineRepository.findById(id);
        if (cuisineOpt.isPresent() && cuisineOpt.get().getVendor().getId().equals(userId)) {
            cuisineRepository.delete(cuisineOpt.get());
        }
        return "redirect:/vendor/cuisines?deleted=true";
    }
}
