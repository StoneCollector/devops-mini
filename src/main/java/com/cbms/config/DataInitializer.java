package com.cbms.config;

import com.cbms.model.CateringSlot;
import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.CateringSlotRepository;
import com.cbms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CateringSlotRepository cateringSlotRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, 
                           CateringSlotRepository cateringSlotRepository, 
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.cateringSlotRepository = cateringSlotRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // 0. Ensure MySQL column 'role' is VARCHAR(30) so 'VENDOR' enum string fits without truncation
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {
        }

        try {
            jdbcTemplate.execute("UPDATE users SET active = 1 WHERE active IS NULL");
        } catch (Exception ignored) {
        }

        // 1. Ensure all existing users have active = true
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.isActive()) {
                user.setActive(true);
                userRepository.save(user);
            }
        }

        // 2. Ensure Admin seeded account exists
        User admin = userRepository.findByEmail("admin@smartcatering.com")
                .orElseGet(() -> {
                    User u = new User("System Admin", "admin@smartcatering.com", "admin123", Role.ADMIN, true);
                    return userRepository.save(u);
                });
        admin.setActive(true);
        userRepository.save(admin);

        // 3. Ensure Vendor seeded account exists
        User vendor = userRepository.findByEmail("chef.mario@smartcatering.com")
                .orElseGet(() -> {
                    User u = new User("Chef Mario Catering", "chef.mario@smartcatering.com", "vendor123", Role.VENDOR, true);
                    return userRepository.save(u);
                });
        vendor.setActive(true);
        userRepository.save(vendor);

        // 4. Ensure Customer seeded account exists
        User customer = userRepository.findByEmail("customer@example.com")
                .orElseGet(() -> {
                    User u = new User("John Doe", "customer@example.com", "customer123", Role.CUSTOMER, true);
                    return userRepository.save(u);
                });
        customer.setActive(true);
        userRepository.save(customer);

        // 5. Ensure existing slots without vendor are assigned to the seeded vendor
        List<CateringSlot> existingSlots = cateringSlotRepository.findAll();
        if (existingSlots.isEmpty()) {
            LocalDate today = LocalDate.now();
            cateringSlotRepository.save(new CateringSlot(today, "Lunch (12:00 PM - 2:00 PM)", "Standard Buffet", 30, vendor));
            cateringSlotRepository.save(new CateringSlot(today, "Dinner (7:00 PM - 9:00 PM)", "Executive Dining", 20, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(1), "Lunch (12:00 PM - 2:00 PM)", "Gourmet Vegetarian", 25, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(1), "Dinner (7:00 PM - 9:00 PM)", "Seafood Special", 15, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(2), "Lunch (12:00 PM - 2:00 PM)", "Continental Spread", 40, vendor));
        } else {
            for (CateringSlot slot : existingSlots) {
                if (slot.getVendor() == null) {
                    slot.setVendor(vendor);
                    cateringSlotRepository.save(slot);
                }
            }
        }
    }
}
