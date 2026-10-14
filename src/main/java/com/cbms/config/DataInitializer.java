package com.cbms.config;

import com.cbms.model.BookingStatus;
import com.cbms.model.CateringEvent;
import com.cbms.model.CateringSlot;
import com.cbms.model.OccasionType;
import com.cbms.model.Role;
import com.cbms.model.ServiceStyle;
import com.cbms.model.ServiceType;
import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import com.cbms.model.VendorProfile;
import com.cbms.model.VendorResourcePool;
import com.cbms.repository.CateringEventRepository;
import com.cbms.repository.CateringSlotRepository;
import com.cbms.repository.UserRepository;
import com.cbms.repository.VendorCuisineRepository;
import com.cbms.repository.VendorProfileRepository;
import com.cbms.repository.VendorResourcePoolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CateringSlotRepository cateringSlotRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorResourcePoolRepository resourcePoolRepository;
    private final VendorCuisineRepository cuisineRepository;
    private final CateringEventRepository eventRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, 
                           CateringSlotRepository cateringSlotRepository, 
                           VendorProfileRepository vendorProfileRepository,
                           VendorResourcePoolRepository resourcePoolRepository,
                           VendorCuisineRepository cuisineRepository,
                           CateringEventRepository eventRepository,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.cateringSlotRepository = cateringSlotRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.resourcePoolRepository = resourcePoolRepository;
        this.cuisineRepository = cuisineRepository;
        this.eventRepository = eventRepository;
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

        // 1. Ensure Admin seeded account exists with contact details
        User admin = userRepository.findByEmail("admin@smartcatering.com")
                .orElseGet(() -> {
                    User u = new User("System Admin", "admin@smartcatering.com", "admin123", Role.ADMIN, true);
                    return userRepository.save(u);
                });
        admin.setActive(true);
        if (admin.getPhoneNumber() == null) admin.setPhoneNumber("9876543200");
        if (admin.getAddressLine() == null) admin.setAddressLine("Admin Operations Center, Level 5");
        if (admin.getCity() == null) admin.setCity("Mumbai");
        if (admin.getPincode() == null) admin.setPincode("400001");
        userRepository.save(admin);

        // 2. Ensure Vendor seeded account exists with contact details
        User vendor = userRepository.findByEmail("chef.mario@smartcatering.com")
                .orElseGet(() -> {
                    User u = new User("Chef Mario Catering", "chef.mario@smartcatering.com", "vendor123", Role.VENDOR, true);
                    return userRepository.save(u);
                });
        vendor.setActive(true);
        if (vendor.getPhoneNumber() == null) vendor.setPhoneNumber("9876543201");
        if (vendor.getAddressLine() == null) vendor.setAddressLine("Plot 14, Kitchen Industrial Zone, Andheri West");
        if (vendor.getCity() == null) vendor.setCity("Mumbai");
        if (vendor.getPincode() == null) vendor.setPincode("400053");
        if (vendor.getOrganizationName() == null) vendor.setOrganizationName("Mario Culinary Group Ltd.");
        userRepository.save(vendor);

        // 3. Ensure Customer seeded account exists with contact details
        User customer = userRepository.findByEmail("customer@example.com")
                .orElseGet(() -> {
                    User u = new User("John Doe", "customer@example.com", "customer123", Role.CUSTOMER, true);
                    return userRepository.save(u);
                });
        customer.setActive(true);
        if (customer.getPhoneNumber() == null) customer.setPhoneNumber("9876543202");
        if (customer.getAddressLine() == null) customer.setAddressLine("42 Marine Lines, Bayview Towers, Apt 7B");
        if (customer.getCity() == null) customer.setCity("Mumbai");
        if (customer.getPincode() == null) customer.setPincode("400020");
        if (customer.getOrganizationName() == null) customer.setOrganizationName("Acme Global Solutions");
        userRepository.save(customer);

        // 4. Seed VendorProfile for Chef Mario
        if (vendorProfileRepository.findByVendor(vendor).isEmpty()) {
            VendorProfile vp = new VendorProfile(
                    vendor,
                    "Chef Mario Grand Catering & Banquets",
                    "9876543201",
                    "Plot 14, Kitchen Industrial Zone, Andheri West, Mumbai"
            );
            vp.setServiceRadius(35);
            vp.setAdvanceBookingDays(2);
            vp.setFssaiLicenseNo("FSSAI-11521000123456");
            vendorProfileRepository.save(vp);
        }

        // 5. Seed VendorResourcePool for Chef Mario (15 staff, 200 utensils, 4 vehicles, 120 venue covers)
        if (resourcePoolRepository.findByVendor(vendor).isEmpty()) {
            VendorResourcePool pool = new VendorResourcePool(vendor, 15, 200, 4, 120);
            resourcePoolRepository.save(pool);
        }

        // 6. Seed VendorCuisines for Chef Mario
        List<VendorCuisine> existingCuisines = cuisineRepository.findByVendorOrderByCuisineNameAsc(vendor);
        if (existingCuisines.isEmpty()) {
            VendorCuisine c1 = new VendorCuisine(
                    vendor,
                    "Royal Awadhi & Mughlai Feast",
                    "BUFFET,PLATED",
                    750.0,
                    20,
                    400,
                    "HALAL",
                    "Signature Dum Biryani, Galouti Kebabs, Sheermal, Paneer Tikka, Shahi Tukda & Rose Phirni."
            );
            VendorCuisine c2 = new VendorCuisine(
                    vendor,
                    "Traditional South Indian Sadya",
                    "BUFFET,FAMILY_STYLE",
                    450.0,
                    25,
                    500,
                    "VEGETARIAN,JAIN",
                    "Authentic Kerala Sadya served with 16 varieties including Avial, Thoran, Kalan, Sambar, Payasam & Appam."
            );
            VendorCuisine c3 = new VendorCuisine(
                    vendor,
                    "Continental Corporate Banquet",
                    "BUFFET,LIVE_STATION",
                    850.0,
                    15,
                    300,
                    "GLUTEN_FREE",
                    "Live artisanal pasta counter, herb-roasted grilled cuts, quinoa harvest salads, artisan focaccia, and classic Tiramisu."
            );
            cuisineRepository.save(c1);
            cuisineRepository.save(c2);
            cuisineRepository.save(c3);
            existingCuisines = List.of(c1, c2, c3);
        }

        // 7. Seed sample CateringEvents demonstrating v2 service models
        List<CateringEvent> existingEvents = eventRepository.findByVendorOrderByEventDateDescCreatedAtDesc(vendor);
        if (existingEvents.isEmpty()) {
            LocalDate sampleDate1 = LocalDate.now().plusDays(3);
            LocalDate sampleDate2 = LocalDate.now().plusDays(5);

            // Event 1: Delivery with Serving
            CateringEvent ev1 = new CateringEvent(
                    customer,
                    vendor,
                    existingCuisines.get(0),
                    OccasionType.CORPORATE,
                    ServiceType.DELIVERY_WITH_SERVING,
                    ServiceStyle.BUFFET,
                    sampleDate1,
                    "12:30",
                    "15:30",
                    60,
                    "Setup buffet line in boardroom 4B by 11:45 AM. 10 Jain requests.",
                    "Acme Global HQ, Tech Park C, Powai, Mumbai",
                    null
            );
            // 60 guests: servers = ceil(60/15) = 4, captain = 1 (since 60 > 30), kitchen = ceil(60/50) = 2 => staff = 7
            ev1.setStaffConsumed(7);
            ev1.setUtensilsConsumed((int) Math.ceil(60 * 1.15)); // 69
            ev1.setVehicleConsumed(1);
            ev1.setVenueCoversConsumed(0);
            ev1.setStatus(BookingStatus.CONFIRMED);
            eventRepository.save(ev1);

            // Event 2: At Venue Wedding Reception
            CateringEvent ev2 = new CateringEvent(
                    customer,
                    vendor,
                    existingCuisines.get(1),
                    OccasionType.WEDDING,
                    ServiceType.AT_VENUE,
                    ServiceStyle.FAMILY_STYLE,
                    sampleDate2,
                    "18:00",
                    "22:30",
                    80,
                    "Mandap decor setup by 4:00 PM. Jasmine floral runners on all dining tables.",
                    null,
                    null
            );
            // 80 guests: servers = ceil(80/15) = 6, captain = 1, kitchen = ceil(80/50) = 2 => staff = 9
            ev2.setStaffConsumed(9);
            ev2.setUtensilsConsumed((int) Math.ceil(80 * 1.15)); // 92
            ev2.setVehicleConsumed(0);
            ev2.setVenueCoversConsumed(80);
            ev2.setStatus(BookingStatus.PENDING);
            eventRepository.save(ev2);
        }

        // 8. Ensure existing slots without vendor are assigned to the seeded vendor
        List<CateringSlot> existingSlots = cateringSlotRepository.findAll();
        for (CateringSlot slot : existingSlots) {
            if (slot.getVendor() == null) {
                slot.setVendor(vendor);
                cateringSlotRepository.save(slot);
            }
        }

        // 9. Ensure at least one upcoming seeded slot with available capacity exists
        LocalDate today = LocalDate.now();
        CateringSlot seededSlot = existingSlots.stream()
                .filter(s -> !s.getDate().isBefore(today) && "Standard Buffet".equals(s.getMenuType()))
                .findFirst()
                .orElse(null);

        if (seededSlot == null) {
            cateringSlotRepository.save(new CateringSlot(today, "Lunch (12:00 PM - 2:00 PM)", "Standard Buffet", 30, vendor));
            cateringSlotRepository.save(new CateringSlot(today, "Dinner (7:00 PM - 9:00 PM)", "Executive Dining", 20, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(1), "Lunch (12:00 PM - 2:00 PM)", "Gourmet Vegetarian", 25, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(1), "Dinner (7:00 PM - 9:00 PM)", "Seafood Special", 15, vendor));
            cateringSlotRepository.save(new CateringSlot(today.plusDays(2), "Lunch (12:00 PM - 2:00 PM)", "Continental Spread", 40, vendor));
        } else if (seededSlot.getAvailableCapacity() < 5) {
            seededSlot.setCapacity(seededSlot.getSlotsBooked() + 30);
            cateringSlotRepository.save(seededSlot);
        }
    }
}
