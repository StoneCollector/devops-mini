package com.cbms.service;

import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.model.VendorProfile;
import com.cbms.repository.UserRepository;
import com.cbms.repository.VendorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;

    public UserService(UserRepository userRepository) {
        this(userRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public UserService(UserRepository userRepository, VendorProfileRepository vendorProfileRepository) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
    }

    public User register(String name, String email, String password) {
        return register(name, email, password, Role.CUSTOMER, null, null, null, null, null, null);
    }

    public User register(String name, String email, String password, Role role) {
        return register(name, email, password, role, null, null, null, null, null, null);
    }

    public User register(String name, String email, String password, Role role,
                         String phoneNumber, String addressLine, String city, String pincode,
                         String organizationName, String gstNumber) {
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

        // Validate mandatory contact info if provided, or provide defaults for backward-compatible calls
        String cleanPhone = (phoneNumber != null && !phoneNumber.trim().isEmpty()) ? phoneNumber.trim() : null;
        String cleanAddress = (addressLine != null && !addressLine.trim().isEmpty()) ? addressLine.trim() : null;
        String cleanCity = (city != null && !city.trim().isEmpty()) ? city.trim() : null;
        String cleanPincode = (pincode != null && !pincode.trim().isEmpty()) ? pincode.trim() : null;

        if (cleanPhone != null) {
            validatePhone(cleanPhone);
        }
        if (cleanPincode != null) {
            validatePincode(cleanPincode);
        }

        Role userRole = (role != null) ? role : Role.CUSTOMER;
        User user = new User(name.trim(), email.trim(), password, userRole);
        user.setPhoneNumber(cleanPhone);
        user.setAddressLine(cleanAddress);
        user.setCity(cleanCity);
        user.setPincode(cleanPincode);
        user.setOrganizationName(organizationName != null && !organizationName.trim().isEmpty() ? organizationName.trim() : null);
        user.setGstNumber(gstNumber != null && !gstNumber.trim().isEmpty() ? gstNumber.trim() : null);

        User savedUser = userRepository.save(user);

        // Auto-create initial VendorProfile if role is VENDOR
        if (userRole == Role.VENDOR && vendorProfileRepository != null) {
            VendorProfile profile = new VendorProfile(
                    savedUser,
                    user.getName(),
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    user.getAddressLine() != null ? user.getAddressLine() : ""
            );
            vendorProfileRepository.save(profile);
        }

        return savedUser;
    }

    public User registerUser(String name, String email, String password, Role role) {
        return register(name, email, password, role);
    }

    public void validateContactDetails(String phoneNumber, String addressLine, String city, String pincode) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        validatePhone(phoneNumber.trim());

        if (addressLine == null || addressLine.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        if (pincode == null || pincode.trim().isEmpty()) {
            throw new IllegalArgumentException("Pincode cannot be empty");
        }
        validatePincode(pincode.trim());
    }

    private void validatePhone(String phone) {
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10) {
            throw new IllegalArgumentException("Phone number must have at least 10 digits");
        }
    }

    private void validatePincode(String pincode) {
        if (!pincode.matches("^[0-9]{5,6}$")) {
            throw new IllegalArgumentException("Pincode must be 5 or 6 digits");
        }
    }

    @Transactional
    public User updateUserProfile(Long userId, String name, String phoneNumber, String addressLine,
                                  String city, String pincode, String organizationName, String gstNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        validateContactDetails(phoneNumber, addressLine, city, pincode);

        user.setName(name.trim());
        user.setPhoneNumber(phoneNumber.trim());
        user.setAddressLine(addressLine.trim());
        user.setCity(city.trim());
        user.setPincode(pincode.trim());
        user.setOrganizationName(organizationName != null && !organizationName.trim().isEmpty() ? organizationName.trim() : null);
        user.setGstNumber(gstNumber != null && !gstNumber.trim().isEmpty() ? gstNumber.trim() : null);

        return userRepository.save(user);
    }

    public Optional<VendorProfile> getVendorProfile(Long vendorId) {
        return vendorProfileRepository.findByVendorId(vendorId);
    }

    public VendorProfile getOrCreateVendorProfile(User vendor) {
        return vendorProfileRepository.findByVendor(vendor)
                .orElseGet(() -> {
                    VendorProfile profile = new VendorProfile(
                            vendor,
                            vendor.getName() != null ? vendor.getName() : "Catering Business",
                            vendor.getPhoneNumber() != null ? vendor.getPhoneNumber() : "",
                            vendor.getAddressLine() != null ? vendor.getAddressLine() : ""
                    );
                    return vendorProfileRepository.save(profile);
                });
    }

    @Transactional
    public VendorProfile saveVendorProfile(VendorProfile profile) {
        if (profile.getBusinessName() == null || profile.getBusinessName().trim().isEmpty()) {
            throw new IllegalArgumentException("Business name cannot be empty");
        }
        return vendorProfileRepository.save(profile);
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
