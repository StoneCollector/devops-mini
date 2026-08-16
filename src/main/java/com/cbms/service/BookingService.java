package com.cbms.service;

import com.cbms.model.Booking;
import com.cbms.model.BookingStatus;
import com.cbms.model.CateringSlot;
import com.cbms.model.User;
import com.cbms.repository.BookingRepository;
import com.cbms.repository.CateringSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CateringSlotRepository slotRepository;

    public BookingService(BookingRepository bookingRepository, CateringSlotRepository slotRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
    }

    public List<CateringSlot> getUpcomingSlots() {
        return slotRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now());
    }
    
    public Optional<CateringSlot> getSlotById(Long id) {
        return slotRepository.findById(id);
    }

    @Transactional
    public Booking createBooking(User user, Long slotId, int guests) {
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

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != com.cbms.model.Role.ADMIN) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        // Restore capacity
        CateringSlot slot = booking.getSlot();
        slot.setSlotsBooked(slot.getSlotsBooked() - booking.getNumberOfGuests());
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
