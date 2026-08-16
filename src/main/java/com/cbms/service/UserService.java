package com.cbms.service;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String name, String email, String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }
        User user = new User(name, email, password, role);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                // In a real app, verify hashed password
                .filter(user -> user.getPassword().equals(password));
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
