package com.cbms.service;

import com.cbms.model.Booking;
import com.cbms.model.BookingStatus;
import com.cbms.model.CateringSlot;
import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.BookingRepository;
import com.cbms.repository.CateringSlotRepository;
import com.cbms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CateringSlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, CateringSlotRepository slotRepository) {
        this(bookingRepository, slotRepository, null);
    }

    @Autowired
    public BookingService(BookingRepository bookingRepository, CateringSlotRepository slotRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

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

        // Restore capacity
        CateringSlot slot = booking.getSlot();
        slot.setSlotsBooked(Math.max(0, slot.getSlotsBooked() - booking.getNumberOfGuests()));
        slotRepository.save(slot);

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

