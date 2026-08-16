package com.cbms.service;

import com.cbms.model.Booking;
import com.cbms.model.CateringSlot;
import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.BookingRepository;
import com.cbms.repository.CateringSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private CateringSlotRepository slotRepository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        slotRepository = Mockito.mock(CateringSlotRepository.class);
        bookingService = new BookingService(bookingRepository, slotRepository);
    }

    @Test
    void testCreateBooking_Success() {
        User user = new User("Alice", "alice@example.com", "pass", Role.CUSTOMER);
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 50);
        slot.setId(1L);

        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        Booking booking = bookingService.createBooking(user, 1L, 10);

        assertNotNull(booking);
        assertEquals(user, booking.getUser());
        assertEquals(slot, booking.getSlot());
        assertEquals(10, booking.getNumberOfGuests());
        assertEquals(10, slot.getSlotsBooked());
        verify(slotRepository).save(slot);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_NotEnoughCapacity() {
        User user = new User("Bob", "bob@example.com", "pass", Role.CUSTOMER);
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Dinner", "Mexican", 20);
        slot.setId(2L);
        slot.setSlotsBooked(15);

        when(slotRepository.findById(2L)).thenReturn(Optional.of(slot));

        assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(user, 2L, 10);
        });
    }
}
