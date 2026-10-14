package com.cbms.service;

import com.cbms.model.Booking;
import com.cbms.model.BookingStatus;
import com.cbms.model.CateringEvent;
import com.cbms.model.CateringSlot;
import com.cbms.model.OccasionType;
import com.cbms.model.Role;
import com.cbms.model.ServiceStyle;
import com.cbms.model.ServiceType;
import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import com.cbms.model.VendorResourcePool;
import com.cbms.repository.BookingRepository;
import com.cbms.repository.CateringEventRepository;
import com.cbms.repository.CateringSlotRepository;
import com.cbms.repository.UserRepository;
import com.cbms.repository.VendorCuisineRepository;
import com.cbms.repository.VendorResourcePoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CateringSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final CateringEventRepository cateringEventRepository;
    private final VendorResourcePoolRepository resourcePoolRepository;
    private final VendorCuisineRepository cuisineRepository;

    public BookingService(BookingRepository bookingRepository, CateringSlotRepository slotRepository) {
        this(bookingRepository, slotRepository, null, null, null, null);
    }

    public BookingService(BookingRepository bookingRepository, CateringSlotRepository slotRepository, UserRepository userRepository) {
        this(bookingRepository, slotRepository, userRepository, null, null, null);
    }

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          CateringSlotRepository slotRepository,
                          UserRepository userRepository,
                          CateringEventRepository cateringEventRepository,
                          VendorResourcePoolRepository resourcePoolRepository,
                          VendorCuisineRepository cuisineRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.cateringEventRepository = cateringEventRepository;
        this.resourcePoolRepository = resourcePoolRepository;
        this.cuisineRepository = cuisineRepository;
    }

    // =========================================================================
    // Part 1: Industry-Standard Multi-Dimensional Resource Pool Logic
    // =========================================================================

    public int calculateStaffNeeded(int guests, ServiceType serviceType) {
        if (serviceType == null || !serviceType.consumesServingStaff() || guests <= 0) {
            return 0;
        }
        int servers = Math.max(1, (int) Math.ceil(guests / 15.0));
        int captain = (guests > 30) ? 1 : 0;
        int kitchen = Math.max(1, (int) Math.ceil(guests / 50.0));
        return servers + captain + kitchen;
    }

    public int calculateUtensilsNeeded(int guests, ServiceType serviceType) {
        if (serviceType == null || !serviceType.consumesUtensilSets() || guests <= 0) {
            return 0;
        }
        return (int) Math.ceil(guests * 1.15);
    }

    public int calculateVehiclesNeeded(ServiceType serviceType) {
        if (serviceType == null || !serviceType.consumesDeliveryVehicle()) {
            return 0;
        }
        return 1;
    }

    public int calculateVenueCoversNeeded(int guests, ServiceType serviceType) {
        if (serviceType == null || !serviceType.consumesVenueCovers() || guests <= 0) {
            return 0;
        }
        return guests;
    }

    public static class DailyResourceUsage {
        private final int staffUsed;
        private final int staffTotal;
        private final int utensilsUsed;
        private final int utensilsTotal;
        private final int vehiclesUsed;
        private final int vehiclesTotal;
        private final int venueCoversUsed;
        private final int venueCoversTotal;

        public DailyResourceUsage(int staffUsed, int staffTotal, int utensilsUsed, int utensilsTotal,
                                  int vehiclesUsed, int vehiclesTotal, int venueCoversUsed, int venueCoversTotal) {
            this.staffUsed = staffUsed;
            this.staffTotal = staffTotal;
            this.utensilsUsed = utensilsUsed;
            this.utensilsTotal = utensilsTotal;
            this.vehiclesUsed = vehiclesUsed;
            this.vehiclesTotal = vehiclesTotal;
            this.venueCoversUsed = venueCoversUsed;
            this.venueCoversTotal = venueCoversTotal;
        }

        public int getStaffUsed() { return staffUsed; }
        public int getStaffTotal() { return staffTotal; }
        public int getStaffAvailable() { return Math.max(0, staffTotal - staffUsed); }

        public int getUtensilsUsed() { return utensilsUsed; }
        public int getUtensilsTotal() { return utensilsTotal; }
        public int getUtensilsAvailable() { return Math.max(0, utensilsTotal - utensilsUsed); }

        public int getVehiclesUsed() { return vehiclesUsed; }
        public int getVehiclesTotal() { return vehiclesTotal; }
        public int getVehiclesAvailable() { return Math.max(0, vehiclesTotal - vehiclesUsed); }

        public int getVenueCoversUsed() { return venueCoversUsed; }
        public int getVenueCoversTotal() { return venueCoversTotal; }
        public int getVenueCoversAvailable() { return Math.max(0, venueCoversTotal - venueCoversUsed); }
    }

    public DailyResourceUsage getResourceUsageOnDate(Long vendorId, LocalDate date) {
        int staffTotal = 12;
        int utensilsTotal = 150;
        int vehiclesTotal = 3;
        int venueCoversTotal = 100;

        if (resourcePoolRepository != null) {
            Optional<VendorResourcePool> poolOpt = resourcePoolRepository.findByVendorId(vendorId);
            if (poolOpt.isPresent()) {
                VendorResourcePool pool = poolOpt.get();
                staffTotal = pool.getStaffCount();
                utensilsTotal = pool.getUtensilSets();
                vehiclesTotal = pool.getDeliveryVehicles();
                venueCoversTotal = pool.getVenueCoverCapacity();
            }
        }

        int staffUsed = 0;
        int utensilsUsed = 0;
        int vehiclesUsed = 0;
        int venueCoversUsed = 0;

        if (cateringEventRepository != null) {
            List<CateringEvent> activeEvents = cateringEventRepository.findActiveEventsForVendorOnDate(vendorId, date, BookingStatus.CANCELLED);
            for (CateringEvent ev : activeEvents) {
                staffUsed += ev.getStaffConsumed();
                utensilsUsed += ev.getUtensilsConsumed();
                vehiclesUsed += ev.getVehicleConsumed();
                venueCoversUsed += ev.getVenueCoversConsumed();
            }
        }

        return new DailyResourceUsage(staffUsed, staffTotal, utensilsUsed, utensilsTotal, vehiclesUsed, vehiclesTotal, venueCoversUsed, venueCoversTotal);
    }

    public void checkResourceFeasibility(User vendor, LocalDate date, ServiceType serviceType, int guests) {
        if (vendor == null || date == null || serviceType == null) {
            throw new IllegalArgumentException("Vendor, date, and service type are required for resource validation");
        }
        if (guests <= 0) {
            throw new IllegalArgumentException("Guest count must be at least 1");
        }

        DailyResourceUsage usage = getResourceUsageOnDate(vendor.getId(), date);

        int staffNeeded = calculateStaffNeeded(guests, serviceType);
        int utensilsNeeded = calculateUtensilsNeeded(guests, serviceType);
        int vehiclesNeeded = calculateVehiclesNeeded(serviceType);
        int venueCoversNeeded = calculateVenueCoversNeeded(guests, serviceType);

        if (venueCoversNeeded > 0 && (usage.getVenueCoversUsed() + venueCoversNeeded) > usage.getVenueCoversTotal()) {
            throw new IllegalStateException("Venue capacity exhausted for " + date + ". Available: " +
                    usage.getVenueCoversAvailable() + " covers, requested: " + venueCoversNeeded + " guests.");
        }

        if (staffNeeded > 0 && (usage.getStaffUsed() + staffNeeded) > usage.getStaffTotal()) {
            throw new IllegalStateException("Serving staff capacity exhausted for " + date + ". Available staff: " +
                    usage.getStaffAvailable() + ", required for " + guests + " guests: " + staffNeeded + " personnel.");
        }

        if (utensilsNeeded > 0 && (usage.getUtensilsUsed() + utensilsNeeded) > usage.getUtensilsTotal()) {
            throw new IllegalStateException("Utensil sets exhausted for " + date + ". Available: " +
                    usage.getUtensilsAvailable() + " sets, required: " + utensilsNeeded + " sets.");
        }

        if (vehiclesNeeded > 0 && (usage.getVehiclesUsed() + vehiclesNeeded) > usage.getVehiclesTotal()) {
            throw new IllegalStateException("Delivery fleet fully booked for " + date + ". Available vehicles: " +
                    usage.getVehiclesAvailable() + ", required: " + vehiclesNeeded + " vehicle.");
        }
    }

    // =========================================================================
    // Part 2: Catering Event Management (v2 Core)
    // =========================================================================

    @Transactional
    public CateringEvent createEventBooking(Long customerId, Long vendorId, Long cuisineId,
                                            OccasionType occasionType, ServiceType serviceType,
                                            ServiceStyle serviceStyle, LocalDate eventDate,
                                            String eventTimeFrom, String eventTimeTo,
                                            int guestCount, String specialNotes,
                                            String deliveryAddress, String pickupTime) {
        if (userRepository == null || cateringEventRepository == null) {
            throw new IllegalStateException("Repositories not initialized");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found with id: " + vendorId));

        if (eventDate == null) {
            throw new IllegalArgumentException("Event date cannot be empty");
        }
        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
        if (guestCount <= 0) {
            throw new IllegalArgumentException("Guest count must be at least 1");
        }

        ServiceType type = (serviceType != null) ? serviceType : ServiceType.DELIVERY_ONLY;

        // Delivery address check
        String effectiveAddress = deliveryAddress;
        if (type.requiresDeliveryAddress()) {
            if (effectiveAddress == null || effectiveAddress.trim().isEmpty()) {
                effectiveAddress = customer.getAddressLine();
            }
            if (effectiveAddress == null || effectiveAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Delivery address is mandatory for " + type.getDisplayName());
            }
        }

        // Takeaway check
        if (type.requiresPickupTime()) {
            if (pickupTime == null || pickupTime.trim().isEmpty()) {
                throw new IllegalArgumentException("Pickup time window is mandatory for Takeaway orders");
            }
        }

        // Cuisine validation
        VendorCuisine cuisine = null;
        if (cuisineId != null && cuisineRepository != null) {
            cuisine = cuisineRepository.findById(cuisineId).orElse(null);
            if (cuisine != null) {
                if (guestCount < cuisine.getMinGuests()) {
                    throw new IllegalArgumentException("Minimum guest requirement for " + cuisine.getCuisineName() + " is " + cuisine.getMinGuests());
                }
                if (guestCount > cuisine.getMaxGuests()) {
                    throw new IllegalArgumentException("Maximum guest capacity for " + cuisine.getCuisineName() + " is " + cuisine.getMaxGuests());
                }
            }
        }

        // Multi-dimensional capacity validation
        checkResourceFeasibility(vendor, eventDate, type, guestCount);

        int staffNeeded = calculateStaffNeeded(guestCount, type);
        int utensilsNeeded = calculateUtensilsNeeded(guestCount, type);
        int vehiclesNeeded = calculateVehiclesNeeded(type);
        int venueCoversNeeded = calculateVenueCoversNeeded(guestCount, type);

        CateringEvent event = new CateringEvent(
                customer, vendor, cuisine, occasionType, type, serviceStyle,
                eventDate, eventTimeFrom, eventTimeTo, guestCount,
                specialNotes, effectiveAddress, pickupTime
        );
        event.setStaffConsumed(staffNeeded);
        event.setUtensilsConsumed(utensilsNeeded);
        event.setVehicleConsumed(vehiclesNeeded);
        event.setVenueCoversConsumed(venueCoversNeeded);

        return cateringEventRepository.save(event);
    }

    public List<CateringEvent> getEventsForVendor(User vendor) {
        if (cateringEventRepository == null || vendor == null) return Collections.emptyList();
        return cateringEventRepository.findByVendorOrderByEventDateDescCreatedAtDesc(vendor);
    }

    public List<CateringEvent> getEventsForCustomer(User customer) {
        if (cateringEventRepository == null || customer == null) return Collections.emptyList();
        return cateringEventRepository.findByCustomerOrderByEventDateDescCreatedAtDesc(customer);
    }

    public List<CateringEvent> getAllEvents() {
        if (cateringEventRepository == null) return Collections.emptyList();
        return cateringEventRepository.findAllByOrderByEventDateDescCreatedAtDesc();
    }

    public Optional<CateringEvent> getEventById(Long eventId) {
        if (cateringEventRepository == null || eventId == null) return Optional.empty();
        return cateringEventRepository.findById(eventId);
    }

    @Transactional
    public CateringEvent confirmEventByVendor(Long eventId, User vendor) {
        CateringEvent event = cateringEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        if (!event.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only confirm bookings on your own catering services");
        }
        if (event.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled event");
        }

        event.setStatus(BookingStatus.CONFIRMED);
        return cateringEventRepository.save(event);
    }

    @Transactional
    public CateringEvent rejectEventByVendor(Long eventId, User vendor) {
        CateringEvent event = cateringEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        if (!event.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only reject bookings on your own catering services");
        }

        event.setStatus(BookingStatus.CANCELLED);
        return cateringEventRepository.save(event);
    }

    @Transactional
    public CateringEvent cancelEventByCustomer(Long eventId, User customer) {
        CateringEvent event = cateringEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + eventId));

        if (!event.getCustomer().getId().equals(customer.getId()) && customer.getRole() != Role.ADMIN) {
            throw new IllegalStateException("You can only cancel your own event bookings");
        }

        event.setStatus(BookingStatus.CANCELLED);
        return cateringEventRepository.save(event);
    }

    // =========================================================================
    // Part 3: Backward-Compatible Slot & Booking Methods
    // =========================================================================

    public List<CateringSlot> listAvailableSlots(LocalDate date) {
        return listAvailableSlots(date, null);
    }

    public List<CateringSlot> listAvailableSlots(LocalDate date, String menuType) {
        boolean hasDate = date != null;
        boolean hasMenu = menuType != null && !menuType.trim().isEmpty();

        if (hasDate && hasMenu) {
            return slotRepository.findByDateAndMenuTypeContainingIgnoreCase(date, menuType.trim());
        } else if (hasDate) {
            return slotRepository.findByDate(date);
        } else if (hasMenu) {
            return slotRepository.findByDateGreaterThanEqualAndMenuTypeContainingIgnoreCaseOrderByDateAsc(LocalDate.now(), menuType.trim());
        } else {
            return getUpcomingSlots();
        }
    }

    public List<CateringSlot> getUpcomingSlots() {
        return slotRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
    }

    public Optional<CateringSlot> getSlotById(Long id) {
        return slotRepository.findById(id);
    }

    @Transactional
    public CateringSlot createSlot(User vendor, CateringSlot slot) {
        if (vendor == null || vendor.getRole() != Role.VENDOR) {
            throw new IllegalStateException("Only vendors can create catering slots");
        }
        if (slot.getDate() == null) {
            throw new IllegalArgumentException("Slot date cannot be empty");
        }
        if (slot.getTimeSlot() == null || slot.getTimeSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("Time slot cannot be empty");
        }
        if (slot.getMenuType() == null || slot.getMenuType().trim().isEmpty()) {
            throw new IllegalArgumentException("Menu type cannot be empty");
        }
        if (slot.getCapacity() <= 0) {
            throw new IllegalArgumentException("Slot capacity must be greater than 0");
        }

        slot.setVendor(vendor);
        return slotRepository.save(slot);
    }

    public List<CateringSlot> getSlotsByVendor(User vendor) {
        if (vendor == null) {
            return List.of();
        }
        return slotRepository.findByVendorOrderByDateAsc(vendor);
    }

    public List<CateringSlot> getSlotsByVendorId(Long vendorId) {
        return slotRepository.findByVendorIdOrderByDateAsc(vendorId);
    }

    @Transactional
    public CateringSlot updateSlot(Long slotId, User vendor, CateringSlot updatedData) {
        CateringSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot ID"));

        if (slot.getVendor() == null || !slot.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only edit your own slots");
        }
        if (updatedData.getCapacity() < slot.getSlotsBooked()) {
            throw new IllegalStateException("Capacity cannot be less than already booked seats (" + slot.getSlotsBooked() + ")");
        }
        if (updatedData.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        slot.setDate(updatedData.getDate());
        slot.setTimeSlot(updatedData.getTimeSlot());
        slot.setMenuType(updatedData.getMenuType());
        slot.setCapacity(updatedData.getCapacity());

        return slotRepository.save(slot);
    }

    @Transactional
    public void deleteSlot(Long slotId, User vendor) {
        CateringSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot ID"));

        if (slot.getVendor() == null || !slot.getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only delete your own slots");
        }
        if (slot.getSlotsBooked() > 0) {
            throw new IllegalStateException("Cannot delete slot with active bookings");
        }

        slotRepository.delete(slot);
    }

    public List<Booking> getBookingsForVendor(User vendor) {
        if (vendor == null) {
            return List.of();
        }
        return bookingRepository.findBySlotVendorOrderByCreatedAtDesc(vendor);
    }

    public List<Booking> getBookingsForVendorId(Long vendorId) {
        return bookingRepository.findBySlotVendorIdOrderByCreatedAtDesc(vendorId);
    }

    @Transactional
    public Booking confirmBookingByVendor(Long bookingId, User vendor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (booking.getSlot() == null || booking.getSlot().getVendor() == null || !booking.getSlot().getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only confirm bookings on your own slots");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled booking");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking rejectBookingByVendor(Long bookingId, User vendor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (booking.getSlot() == null || booking.getSlot().getVendor() == null || !booking.getSlot().getVendor().getId().equals(vendor.getId())) {
            throw new IllegalStateException("You can only reject bookings on your own slots");
        }

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            CateringSlot slot = booking.getSlot();
            slot.setSlotsBooked(Math.max(0, slot.getSlotsBooked() - booking.getNumberOfGuests()));
            slotRepository.save(slot);

            booking.setStatus(BookingStatus.CANCELLED);
            return bookingRepository.save(booking);
        }

        return booking;
    }

    @Transactional
    public Booking createBooking(Long userId, Long slotId, int numberOfGuests) {
        if (userRepository == null) {
            throw new IllegalStateException("UserRepository not initialized in BookingService");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return createBooking(user, slotId, numberOfGuests);
    }

    @Transactional
    public Booking createBooking(User user, Long slotId, int guests) {
        if (guests <= 0) {
            throw new IllegalArgumentException("Number of guests must be at least 1");
        }

        CateringSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid slot ID"));

        if (slot.getAvailableCapacity() < guests) {
            throw new IllegalStateException("Not enough capacity in the selected slot");
        }

        slot.setSlotsBooked(slot.getSlotsBooked() + guests);
        slotRepository.save(slot);

        Booking booking = new Booking(user, slot, guests);
        return bookingRepository.save(booking);
    }

    public List<Booking> listBookingsForUser(Long userId) {
        if (userRepository != null) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return getUserBookings(user.get());
            }
        }
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Booking> listAllBookings() {
        return getAllBookings();
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (userRepository != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
            cancelBooking(bookingId, user);
            return;
        }

        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        performCancellation(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        performCancellation(booking);
    }

    private void performCancellation(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        CateringSlot slot = booking.getSlot();
        if (slot != null) {
            slot.setSlotsBooked(Math.max(0, slot.getSlotsBooked() - booking.getNumberOfGuests()));
            slotRepository.save(slot);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled booking");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }
}
