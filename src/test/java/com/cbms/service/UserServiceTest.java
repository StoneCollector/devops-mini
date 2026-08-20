package com.cbms.service;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "password123", Role.CUSTOMER);
        testUser.setId(1L);
    }

    @Test
    void register_NewUniqueEmail_Succeeds() {
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        User registered = userService.register("New User", "newuser@example.com", "securepass");

        assertNotNull(registered);
        assertEquals("New User", registered.getName());
        assertEquals("newuser@example.com", registered.getEmail());
        assertEquals("securepass", registered.getPassword());
        assertEquals(Role.CUSTOMER, registered.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_FailsWithExpectedException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("John Clone", "john@example.com", "password123")
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_CorrectCredentials_Succeeds() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.authenticate("john@example.com", "password123");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void authenticate_WrongPassword_Fails() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.authenticate("john@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
    }
}
