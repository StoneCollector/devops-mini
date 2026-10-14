package com.cbms.controller;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.model.VendorProfile;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String redirect, HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/slots";
        }
        model.addAttribute("redirect", redirect);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false, defaultValue = "CUSTOMER") String role,
                           @RequestParam(required = false) String phoneNumber,
                           @RequestParam(required = false) String addressLine,
                           @RequestParam(required = false) String city,
                           @RequestParam(required = false) String pincode,
                           @RequestParam(required = false) String organizationName,
                           @RequestParam(required = false) String gstNumber,
                           @RequestParam(required = false) String redirect,
                           Model model) {
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("addressLine", addressLine);
        model.addAttribute("city", city);
        model.addAttribute("pincode", pincode);
        model.addAttribute("organizationName", organizationName);
        model.addAttribute("gstNumber", gstNumber);
        model.addAttribute("redirect", redirect);

        try {
            Role userRole;
            try {
                userRole = (role != null && !role.isBlank()) ? Role.valueOf(role.toUpperCase()) : Role.CUSTOMER;
                if (userRole == Role.ADMIN) {
                    userRole = Role.CUSTOMER;
                }
            } catch (IllegalArgumentException e) {
                userRole = Role.CUSTOMER;
            }

            userService.register(name, email, password, userRole, phoneNumber, addressLine, city, pincode, organizationName, gstNumber);
            String target = "/login?registered=true";
            if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
                target += "&redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8);
            }
            return "redirect:" + target;
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            model.addAttribute("error", msg);
            if (msg.contains("Name")) {
                model.addAttribute("nameError", msg);
            } else if (msg.contains("Email") || msg.contains("email")) {
                model.addAttribute("emailError", msg);
            } else if (msg.contains("Password") || msg.contains("password")) {
                model.addAttribute("passwordError", msg);
            } else if (msg.contains("Phone") || msg.contains("phone")) {
                model.addAttribute("phoneError", msg);
            } else if (msg.contains("Address")) {
                model.addAttribute("addressError", msg);
            } else if (msg.contains("City")) {
                model.addAttribute("cityError", msg);
            } else if (msg.contains("Pincode")) {
                model.addAttribute("pincodeError", msg);
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("userRole");
            if ("VENDOR".equals(role)) return "redirect:/vendor/slots";
            if ("ADMIN".equals(role)) return "redirect:/admin";
            return "redirect:/slots";
        }
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                        @RequestParam String password, 
                        @RequestParam(required = false) String redirect,
                        HttpSession session, 
                        Model model) {
        model.addAttribute("email", email);
        model.addAttribute("redirect", redirect);

        if (userService.isAccountDeactivated(email)) {
            model.addAttribute("error", "Your account has been deactivated. Please contact support.");
            return "login";
        }

        Optional<User> user = userService.authenticate(email, password);
        if (user.isPresent()) {
            User loggedIn = user.get();
            session.setAttribute("userId", loggedIn.getId());
            session.setAttribute("userRole", loggedIn.getRole().name());
            session.setAttribute("userName", loggedIn.getName());

            if (redirect != null && !redirect.isBlank() && redirect.startsWith("/")) {
                return "redirect:" + redirect;
            }

            if (loggedIn.getRole() == Role.VENDOR) {
                return "redirect:/vendor/slots";
            } else if (loggedIn.getRole() == Role.ADMIN) {
                return "redirect:/admin";
            }
            return "redirect:/slots";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        if (user.getRole() == Role.VENDOR) {
            VendorProfile vendorProfile = userService.getOrCreateVendorProfile(user);
            model.addAttribute("vendorProfile", vendorProfile);
        }

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String phoneNumber,
                                @RequestParam String addressLine,
                                @RequestParam String city,
                                @RequestParam String pincode,
                                @RequestParam(required = false) String organizationName,
                                @RequestParam(required = false) String gstNumber,
                                // Vendor specific fields
                                @RequestParam(required = false) String businessName,
                                @RequestParam(required = false) String businessPhone,
                                @RequestParam(required = false) String businessAddress,
                                @RequestParam(required = false) Integer serviceRadius,
                                @RequestParam(required = false) String fssaiLicenseNo,
                                @RequestParam(required = false) Integer advanceBookingDays,
                                @RequestParam(required = false) String openingTime,
                                @RequestParam(required = false) String closingTime,
                                @RequestParam(required = false) String takeawayEnabled,
                                @RequestParam(required = false) String readyToDineEnabled,
                                @RequestParam(required = false) String readyToDineStartTime,
                                @RequestParam(required = false) String readyToDineEndTime,
                                HttpSession session,
                                Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User updatedUser = userService.updateUserProfile(userId, name, phoneNumber, addressLine, city, pincode, organizationName, gstNumber);
            session.setAttribute("userName", updatedUser.getName());

            if (updatedUser.getRole() == Role.VENDOR && businessName != null) {
                VendorProfile vp = userService.getOrCreateVendorProfile(updatedUser);
                vp.setBusinessName(businessName.trim());
                vp.setBusinessPhone(businessPhone != null ? businessPhone.trim() : phoneNumber);
                vp.setBusinessAddress(businessAddress != null ? businessAddress.trim() : addressLine);
                if (serviceRadius != null) vp.setServiceRadius(serviceRadius);
                if (fssaiLicenseNo != null) vp.setFssaiLicenseNo(fssaiLicenseNo.trim());
                if (advanceBookingDays != null) vp.setAdvanceBookingDays(advanceBookingDays);

                if (openingTime != null && !openingTime.isBlank()) {
                    try { vp.setOpeningTime(LocalTime.parse(openingTime.trim())); } catch (Exception ignored) {}
                }
                if (closingTime != null && !closingTime.isBlank()) {
                    try { vp.setClosingTime(LocalTime.parse(closingTime.trim())); } catch (Exception ignored) {}
                }
                vp.setTakeawayEnabled("on".equalsIgnoreCase(takeawayEnabled) || "true".equalsIgnoreCase(takeawayEnabled));
                vp.setReadyToDineEnabled("on".equalsIgnoreCase(readyToDineEnabled) || "true".equalsIgnoreCase(readyToDineEnabled));
                if (readyToDineStartTime != null && !readyToDineStartTime.isBlank()) {
                    try { vp.setReadyToDineStartTime(LocalTime.parse(readyToDineStartTime.trim())); } catch (Exception ignored) {}
                }
                if (readyToDineEndTime != null && !readyToDineEndTime.isBlank()) {
                    try { vp.setReadyToDineEndTime(LocalTime.parse(readyToDineEndTime.trim())); } catch (Exception ignored) {}
                }

                userService.saveVendorProfile(vp);
            }

            return "redirect:/profile?updated=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            Optional<User> userOpt = userService.findById(userId);
            userOpt.ifPresent(u -> model.addAttribute("user", u));
            if (userOpt.isPresent() && userOpt.get().getRole() == Role.VENDOR) {
                model.addAttribute("vendorProfile", userService.getOrCreateVendorProfile(userOpt.get()));
            }
            return "profile";
        }
    }
}
