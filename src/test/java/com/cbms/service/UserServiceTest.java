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

    @Mock
    private com.cbms.repository.VendorProfileRepository vendorProfileRepository;

    @Mock
    private com.cbms.repository.VendorCuisineRepository vendorCuisineRepository;

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

    @Test
    void register_WithContactInfo_SucceedsAndCreatesVendorProfileForVendor() {
        when(userRepository.findByEmail("vendor.pro@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User vendor = userService.register("Pro Chef", "vendor.pro@example.com", "secure123", Role.VENDOR,
                "9876543210", "42 Commercial Kitchen Rd", "Pune", "411001", "Pro Culinary Co", "27ABCDE1234F1Z5");

        assertNotNull(vendor);
        assertEquals("9876543210", vendor.getPhoneNumber());
        assertEquals("Pune", vendor.getCity());
        assertEquals("411001", vendor.getPincode());
        assertEquals("Pro Culinary Co", vendor.getOrganizationName());
        verify(vendorProfileRepository, times(1)).save(any(com.cbms.model.VendorProfile.class));
    }

    @Test
    void register_WithShortPhoneNumber_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("Short Phone", "short@example.com", "secure123", Role.CUSTOMER,
                        "12345", "Street", "City", "400001", null, null));
    }

    @Test
    void register_WithInvalidPincode_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.register("Bad Pin", "badpin@example.com", "secure123", Role.CUSTOMER,
                        "9876543210", "Street", "City", "INVALID_PIN", null, null));
    }

    @Test
    void updateUserProfile_ValidDetails_UpdatesSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUserProfile(1L, "John Doe Updated", "9988776655", "New Address", "Delhi", "110001", "New Org", "GST1234");

        assertEquals("John Doe Updated", updated.getName());
        assertEquals("9988776655", updated.getPhoneNumber());
        assertEquals("Delhi", updated.getCity());
        assertEquals("110001", updated.getPincode());
    }

    @Test
    void getAvailableVendors_OpenNowVendor_IsReturned() {
        when(userRepository.findAllByOrderByRoleAscNameAsc()).thenReturn(List.of(testVendor));

        com.cbms.model.VendorProfile profile = new com.cbms.model.VendorProfile(testVendor, "Chef Mario Kitchen", "9876543201", "Mumbai");
        profile.setOpeningTime(java.time.LocalTime.of(8, 0));
        profile.setClosingTime(java.time.LocalTime.of(23, 59));
        profile.setTakeawayEnabled(true);
        profile.setReadyToDineEnabled(true);

        when(vendorProfileRepository.findByVendor(testVendor)).thenReturn(Optional.of(profile));
        when(vendorCuisineRepository.findByVendorAndActiveTrue(testVendor)).thenReturn(List.of());

        java.time.LocalDate testDate = java.time.LocalDate.of(2026, 10, 14); // Wednesday
        java.time.LocalTime testTime = java.time.LocalTime.of(15, 0); // 3 PM

        List<com.cbms.dto.AvailableVendorView> results = userService.getAvailableVendors(testDate, testTime, "all", null);

        assertEquals(1, results.size());
        assertEquals("Chef Mario Kitchen", results.get(0).getProfile().getBusinessName());
        assertTrue(results.get(0).isOpenNow());
        assertTrue(results.get(0).isTakeawayActive());
    }

    @Test
    void getAvailableVendors_VendorClosedBeforeCurrentTime_IsNotVisible() {
        User morningVendor = new User("Sunrise Cafe", "sunrise@example.com", "pass123", Role.VENDOR, true);
        morningVendor.setId(3L);

        when(userRepository.findAllByOrderByRoleAscNameAsc()).thenReturn(List.of(morningVendor));

        com.cbms.model.VendorProfile profile = new com.cbms.model.VendorProfile(morningVendor, "Sunrise Cafe", "9876543206", "Colaba");
        profile.setOpeningTime(java.time.LocalTime.of(6, 0));
        profile.setClosingTime(java.time.LocalTime.of(14, 0)); // Closed at 2 PM

        when(vendorProfileRepository.findByVendor(morningVendor)).thenReturn(Optional.of(profile));

        java.time.LocalDate testDate = java.time.LocalDate.of(2026, 10, 14);
        java.time.LocalTime testTime = java.time.LocalTime.of(20, 0); // 8 PM (after 2 PM closing time)

        List<com.cbms.dto.AvailableVendorView> results = userService.getAvailableVendors(testDate, testTime, "all", null);

        // Vendor closed before current time must not be visible
        assertTrue(results.isEmpty(), "Vendor who closed before current time must not be visible");
    }

    @Test
    void getAvailableVendors_ReadyToDineFilter_ExcludesTakeawayOnlyVendors() {
        when(userRepository.findAllByOrderByRoleAscNameAsc()).thenReturn(List.of(testVendor));

        com.cbms.model.VendorProfile profile = new com.cbms.model.VendorProfile(testVendor, "Takeaway Express", "9876543201", "Mumbai");
        profile.setOpeningTime(java.time.LocalTime.of(8, 0));
        profile.setClosingTime(java.time.LocalTime.of(23, 59));
        profile.setTakeawayEnabled(true);
        profile.setReadyToDineEnabled(false); // No dine-in!

        when(vendorProfileRepository.findByVendor(testVendor)).thenReturn(Optional.of(profile));

        java.time.LocalDate testDate = java.time.LocalDate.of(2026, 10, 14);
        java.time.LocalTime testTime = java.time.LocalTime.of(12, 0);

        List<com.cbms.dto.AvailableVendorView> results = userService.getAvailableVendors(testDate, testTime, "dinein", null);

        // Filter for dine-in should exclude vendor who only offers takeaway
        assertTrue(results.isEmpty());
    }

    @Test
    void vendorProfile_isOperatingAt_EdgeCases() {
        com.cbms.model.VendorProfile vp = new com.cbms.model.VendorProfile();
        vp.setOpeningTime(java.time.LocalTime.of(9, 0));
        vp.setClosingTime(java.time.LocalTime.of(22, 0));
        vp.setOperationalDays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY");

        java.time.LocalDate wednesday = java.time.LocalDate.of(2026, 10, 14);
        java.time.LocalDate sunday = java.time.LocalDate.of(2026, 10, 18);

        // Within operational window on Wednesday
        assertTrue(vp.isOperatingAt(wednesday, java.time.LocalTime.of(14, 0)));

        // Before opening time
        assertFalse(vp.isOperatingAt(wednesday, java.time.LocalTime.of(8, 30)));

        // After closing time (open before current time)
        assertFalse(vp.isOperatingAt(wednesday, java.time.LocalTime.of(22, 30)));

        // Outside operational days (Sunday)
        assertFalse(vp.isOperatingAt(sunday, java.time.LocalTime.of(14, 0)));
    }
}
