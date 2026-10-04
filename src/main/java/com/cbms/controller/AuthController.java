package com.cbms.controller;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/slots";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false, defaultValue = "CUSTOMER") String role,
                           Model model) {
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("role", role);

        try {
            Role userRole;
            try {
                userRole = (role != null && !role.isBlank()) ? Role.valueOf(role.toUpperCase()) : Role.CUSTOMER;
                if (userRole == Role.ADMIN) {
                    // Do not allow public self-registration as admin
                    userRole = Role.CUSTOMER;
                }
            } catch (IllegalArgumentException e) {
                userRole = Role.CUSTOMER;
            }

            userService.register(name, email, password, userRole);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            model.addAttribute("error", msg);
            if (msg.contains("Name")) {
                model.addAttribute("nameError", msg);
            } else if (msg.contains("Email") || msg.contains("email")) {
                model.addAttribute("emailError", msg);
            } else if (msg.contains("Password") || msg.contains("password")) {
                model.addAttribute("passwordError", msg);
            }
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred during registration. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) {
            String role = (String) session.getAttribute("userRole");
            if ("VENDOR".equals(role)) return "redirect:/vendor/slots";
            if ("ADMIN".equals(role)) return "redirect:/admin";
            return "redirect:/slots";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                        @RequestParam String password, 
                        HttpSession session, 
                        Model model) {
        model.addAttribute("email", email);

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
}
