package com.cbms.service;

import com.cbms.model.Booking;
import com.cbms.model.BookingStatus;
import com.cbms.model.CateringSlot;
import com.cbms.model.Role;
import com.cbms.model.User;
import com.cbms.repository.BookingRepository;
import com.cbms.repository.CateringSlotRepository;
import com.cbms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private CateringSlotRepository slotRepository;
    private UserRepository userRepository;
    private BookingService bookingService;

    private User vendor1;
    private User vendor2;
    private User customer;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        slotRepository = Mockito.mock(CateringSlotRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        bookingService = new BookingService(bookingRepository, slotRepository, userRepository);

        vendor1 = new User("Chef Mario", "mario@example.com", "pass123", Role.VENDOR, true);
        vendor1.setId(10L);

        vendor2 = new User("Chef Luigi", "luigi@example.com", "pass123", Role.VENDOR, true);
        vendor2.setId(20L);

        customer = new User("Alice Smith", "alice@example.com", "pass123", Role.CUSTOMER, true);
        customer.setId(100L);
    }

    @Test
    void testVendorCreateSlotSucceeds() {
        CateringSlot newSlot = new CateringSlot(LocalDate.now().plusDays(1), "Lunch", "Italian Buffet", 30);

        when(slotRepository.save(any(CateringSlot.class))).thenAnswer(i -> {
            CateringSlot s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        CateringSlot created = bookingService.createSlot(vendor1, newSlot);

        assertNotNull(created);
        assertEquals(vendor1, created.getVendor());
        assertEquals("Italian Buffet", created.getMenuType());
        assertEquals(30, created.getCapacity());
        verify(slotRepository, times(1)).save(newSlot);
    }

    @Test
    void testNonVendorCannotCreateSlot() {
        CateringSlot newSlot = new CateringSlot(LocalDate.now().plusDays(1), "Dinner", "Continental", 20);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.createSlot(customer, newSlot);
        });

        assertEquals("Only vendors can create catering slots", ex.getMessage());
        verify(slotRepository, never()).save(any(CateringSlot.class));
    }

    @Test
    void testVendorGetOwnSlotsReturnsOnlyVendorSlots() {
        CateringSlot slot1 = new CateringSlot(LocalDate.now(), "Lunch", "Buffet", 30, vendor1);
        slot1.setId(1L);

        when(slotRepository.findByVendorOrderByDateAsc(vendor1)).thenReturn(List.of(slot1));

        List<CateringSlot> slots = bookingService.getSlotsByVendor(vendor1);

        assertEquals(1, slots.size());
        assertEquals(vendor1, slots.get(0).getVendor());
        verify(slotRepository, times(1)).findByVendorOrderByDateAsc(vendor1);
    }

    @Test
    void testVendorUpdateSlotSucceeds() {
        CateringSlot existing = new CateringSlot(LocalDate.now().plusDays(2), "Lunch", "Buffet", 30, vendor1);
        existing.setId(5L);
        existing.setSlotsBooked(5);

        when(slotRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(slotRepository.save(any(CateringSlot.class))).thenAnswer(i -> i.getArgument(0));

        CateringSlot updateData = new CateringSlot(LocalDate.now().plusDays(2), "Dinner", "Gourmet Buffet", 40);
        CateringSlot updated = bookingService.updateSlot(5L, vendor1, updateData);

        assertEquals("Dinner", updated.getTimeSlot());
        assertEquals("Gourmet Buffet", updated.getMenuType());
        assertEquals(40, updated.getCapacity());
        verify(slotRepository, times(1)).save(existing);
    }

    @Test
    void testVendorUpdateAnotherVendorSlotFails() {
        CateringSlot existing = new CateringSlot(LocalDate.now().plusDays(2), "Lunch", "Buffet", 30, vendor2);
        existing.setId(6L);

        when(slotRepository.findById(6L)).thenReturn(Optional.of(existing));

        CateringSlot updateData = new CateringSlot(LocalDate.now().plusDays(2), "Dinner", "Gourmet", 40);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.updateSlot(6L, vendor1, updateData);
        });

        assertEquals("You can only edit your own slots", ex.getMessage());
        verify(slotRepository, never()).save(existing);
    }

    @Test
    void testVendorDeleteSlotSucceedsWhenNoBookings() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 25, vendor1);
        slot.setId(7L);
        slot.setSlotsBooked(0);

        when(slotRepository.findById(7L)).thenReturn(Optional.of(slot));

        bookingService.deleteSlot(7L, vendor1);

        verify(slotRepository, times(1)).delete(slot);
    }

    @Test
    void testVendorDeleteSlotWithActiveBookingsFails() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 25, vendor1);
        slot.setId(8L);
        slot.setSlotsBooked(10); // Active bookings exist

        when(slotRepository.findById(8L)).thenReturn(Optional.of(slot));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.deleteSlot(8L, vendor1);
        });

        assertEquals("Cannot delete slot with active bookings", ex.getMessage());
        verify(slotRepository, never()).delete(slot);
    }

    @Test
    void testVendorConfirmBookingOnOwnSlotSucceeds() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 30, vendor1);
        slot.setId(1L);

        Booking booking = new Booking(customer, slot, 5);
        booking.setId(101L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(101L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking confirmed = bookingService.confirmBookingByVendor(101L, vendor1);

        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testVendorConfirmBookingOnAnotherVendorSlotFails() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 30, vendor2);
        slot.setId(2L);

        Booking booking = new Booking(customer, slot, 5);
        booking.setId(102L);

        when(bookingRepository.findById(102L)).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bookingService.confirmBookingByVendor(102L, vendor1);
        });

        assertEquals("You can only confirm bookings on your own slots", ex.getMessage());
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void testVendorRejectBookingRestoresCapacity() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 30, vendor1);
        slot.setId(1L);
        slot.setSlotsBooked(10);

        Booking booking = new Booking(customer, slot, 5);
        booking.setId(103L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(103L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking rejected = bookingService.rejectBookingByVendor(103L, vendor1);

        assertEquals(BookingStatus.CANCELLED, rejected.getStatus());
        assertEquals(5, slot.getSlotsBooked()); // 10 - 5 = 5
        verify(slotRepository, times(1)).save(slot);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testBookingSlotWithCapacitySucceeds() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Lunch", "Italian", 50, vendor1);
        slot.setId(1L);

        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        Booking booking = bookingService.createBooking(customer, 1L, 10);

        assertNotNull(booking);
        assertEquals(customer, booking.getUser());
        assertEquals(slot, booking.getSlot());
        assertEquals(10, booking.getNumberOfGuests());
        assertEquals(10, slot.getSlotsBooked());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        verify(slotRepository).save(slot);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testBookingFullSlotFails() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Dinner", "Mexican", 20, vendor1);
        slot.setId(2L);
        slot.setSlotsBooked(20); // Fully booked

        when(slotRepository.findById(2L)).thenReturn(Optional.of(slot));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(customer, 2L, 5);
        });

        assertEquals("Not enough capacity in the selected slot", exception.getMessage());
    }

    @Test
    void testBookingNonPositiveGuestsFails() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(customer, 2L, 0);
        });
        assertEquals("Number of guests must be at least 1", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(customer, 2L, -3);
        });
        assertEquals("Number of guests must be at least 1", ex2.getMessage());
    }

    @Test
    void testCancelBookingRestoresCapacity() {
        CateringSlot slot = new CateringSlot(LocalDate.now(), "Dinner", "Buffet", 30, vendor1);
        slot.setId(3L);
        slot.setSlotsBooked(15);

        Booking booking = new Booking(customer, slot, 5);
        booking.setId(50L);
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(50L, customer);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(10, slot.getSlotsBooked()); // 15 - 5 = 10
        verify(slotRepository).save(slot);
        verify(bookingRepository).save(booking);
    }
}
