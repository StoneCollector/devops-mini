package com.cbms.service;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String name, String email, String password) {
        return register(name, email, password, Role.CUSTOMER);
    }

    public User register(String name, String email, String password, Role role) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        Role userRole = (role != null) ? role : Role.CUSTOMER;
        User user = new User(name.trim(), email.trim(), password, userRole);
        return userRepository.save(user);
    }

    public User registerUser(String name, String email, String password, Role role) {
        return register(name, email, password, role);
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim())
                .filter(user -> user.getPassword().equals(password) && user.isActive());
    }

    public boolean isAccountDeactivated(String email) {
        if (email == null) {
            return false;
        }
        return userRepository.findByEmail(email.trim())
                .map(user -> !user.isActive())
                .orElse(false);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByRoleAscNameAsc();
    }

    public User toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}

