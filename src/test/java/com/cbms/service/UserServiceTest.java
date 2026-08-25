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

import java.util.List;
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

    private User testCustomer;
    private User testVendor;

    @BeforeEach
    void setUp() {
        testCustomer = new User("John Doe", "john@example.com", "password123", Role.CUSTOMER, true);
        testCustomer.setId(1L);

        testVendor = new User("Chef Mario", "chef.mario@example.com", "vendor123", Role.VENDOR, true);
        testVendor.setId(2L);
    }

    @Test
    void register_NewUniqueCustomer_Succeeds() {
        when(userRepository.findByEmail("newcustomer@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        User registered = userService.register("New Customer", "newcustomer@example.com", "securepass123", Role.CUSTOMER);

        assertNotNull(registered);
        assertEquals("New Customer", registered.getName());
        assertEquals("newcustomer@example.com", registered.getEmail());
        assertEquals("securepass123", registered.getPassword());
        assertEquals(Role.CUSTOMER, registered.getRole());
        assertTrue(registered.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_NewUniqueVendor_Succeeds() {
        when(userRepository.findByEmail("newvendor@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(11L);
            return u;
        });

        User registered = userService.register("New Vendor", "newvendor@example.com", "securepass123", Role.VENDOR);

        assertNotNull(registered);
        assertEquals(Role.VENDOR, registered.getRole());
        assertTrue(registered.isActive());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_MalformedEmail_FailsWithExpectedException() {
        IllegalArgumentException ex1 = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Jane Doe", "invalid-email", "password123", Role.CUSTOMER)
        );
        assertEquals("Invalid email format", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Jane Doe", "jane@", "password123", Role.CUSTOMER)
        );
        assertEquals("Invalid email format", ex2.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_TooShortPassword_FailsWithExpectedException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("Short Pass", "short@example.com", "12345", Role.CUSTOMER)
        );

        assertEquals("Password must be at least 6 characters long", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_FailsWithExpectedException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testCustomer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register("John Clone", "john@example.com", "password123")
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_CorrectCredentials_Succeeds() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testCustomer));

        Optional<User> result = userService.authenticate("john@example.com", "password123");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void authenticate_WrongPassword_Fails() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testCustomer));

        Optional<User> result = userService.authenticate("john@example.com", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_DeactivatedUser_Fails() {
        testCustomer.setActive(false);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testCustomer));

        Optional<User> result = userService.authenticate("john@example.com", "password123");

        assertTrue(result.isEmpty());
        assertTrue(userService.isAccountDeactivated("john@example.com"));
    }

    @Test
    void toggleUserActive_FlipsActiveState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.toggleUserActive(1L);

        assertFalse(updated.isActive());
        verify(userRepository, times(1)).save(testCustomer);
    }
}
