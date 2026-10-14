package com.cbms.service;

import com.cbms.model.*;
import com.cbms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private CateringSlotRepository slotRepository;
    private UserRepository userRepository;
    private CateringEventRepository cateringEventRepository;
    private VendorResourcePoolRepository resourcePoolRepository;
    private VendorCuisineRepository cuisineRepository;
    private BookingService bookingService;

    private User vendor1;
    private User vendor2;
    private User customer;

    @BeforeEach
    void setUp() {
        bookingRepository = Mockito.mock(BookingRepository.class);
        slotRepository = Mockito.mock(CateringSlotRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        cateringEventRepository = Mockito.mock(CateringEventRepository.class);
        resourcePoolRepository = Mockito.mock(VendorResourcePoolRepository.class);
        cuisineRepository = Mockito.mock(VendorCuisineRepository.class);

        bookingService = new BookingService(bookingRepository, slotRepository, userRepository,
                cateringEventRepository, resourcePoolRepository, cuisineRepository);

        vendor1 = new User("Chef Mario", "mario@example.com", "pass123", Role.VENDOR, true);
        vendor1.setId(10L);
        vendor1.setPhoneNumber("9876543201");
        vendor1.setAddressLine("Andheri West");
        vendor1.setCity("Mumbai");
        vendor1.setPincode("400053");

        vendor2 = new User("Chef Luigi", "luigi@example.com", "pass123", Role.VENDOR, true);
        vendor2.setId(20L);

        customer = new User("Alice Smith", "alice@example.com", "pass123", Role.CUSTOMER, true);
        customer.setId(100L);
        customer.setPhoneNumber("9876543202");
        customer.setAddressLine("Bandra West");
        customer.setCity("Mumbai");
        customer.setPincode("400050");
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

    // =========================================================================
    // v2 Multi-Dimensional Resource Pool & Event Tests
    // =========================================================================

    @Test
    void testCalculateStaffNeeded_IndustryRatios() {
        // 15 guests DELIVERY_WITH_SERVING: 1 server + 0 captain + 1 kitchen = 2
        assertEquals(2, bookingService.calculateStaffNeeded(15, ServiceType.DELIVERY_WITH_SERVING));

        // 30 guests: ceil(30/15) = 2 servers + 0 captain (since not > 30) + ceil(30/50)=1 kitchen = 3
        assertEquals(3, bookingService.calculateStaffNeeded(30, ServiceType.DELIVERY_WITH_SERVING));

        // 60 guests: ceil(60/15) = 4 servers + 1 captain (60 > 30) + ceil(60/50)=2 kitchen = 7
        assertEquals(7, bookingService.calculateStaffNeeded(60, ServiceType.DELIVERY_WITH_SERVING));

        // 100 guests AT_VENUE: ceil(100/15)=7 + 1 captain + ceil(100/50)=2 kitchen = 10
        assertEquals(10, bookingService.calculateStaffNeeded(100, ServiceType.AT_VENUE));

        // DELIVERY_ONLY and TAKEAWAY do not consume serving staff
        assertEquals(0, bookingService.calculateStaffNeeded(100, ServiceType.DELIVERY_ONLY));
        assertEquals(0, bookingService.calculateStaffNeeded(100, ServiceType.TAKEAWAY));
    }

    @Test
    void testCalculateUtensilsNeeded_15PercentBuffer() {
        // 50 guests * 1.15 = 57.5 -> 58 sets
        assertEquals(58, bookingService.calculateUtensilsNeeded(50, ServiceType.DELIVERY_WITH_SERVING));
        assertEquals(58, bookingService.calculateUtensilsNeeded(50, ServiceType.AT_VENUE));

        // 100 guests * 1.15 = 115 sets
        assertEquals(115, bookingService.calculateUtensilsNeeded(100, ServiceType.DELIVERY_WITH_SERVING));

        // DELIVERY_ONLY and TAKEAWAY do not consume utensil inventory
        assertEquals(0, bookingService.calculateUtensilsNeeded(100, ServiceType.DELIVERY_ONLY));
        assertEquals(0, bookingService.calculateUtensilsNeeded(100, ServiceType.TAKEAWAY));
    }

    @Test
    void testCalculateVehiclesNeeded() {
        assertEquals(1, bookingService.calculateVehiclesNeeded(ServiceType.DELIVERY_ONLY));
        assertEquals(1, bookingService.calculateVehiclesNeeded(ServiceType.DELIVERY_WITH_SERVING));
        assertEquals(0, bookingService.calculateVehiclesNeeded(ServiceType.AT_VENUE));
        assertEquals(0, bookingService.calculateVehiclesNeeded(ServiceType.TAKEAWAY));
    }

    @Test
    void testCalculateVenueCoversNeeded() {
        assertEquals(80, bookingService.calculateVenueCoversNeeded(80, ServiceType.AT_VENUE));
        assertEquals(0, bookingService.calculateVenueCoversNeeded(80, ServiceType.DELIVERY_ONLY));
        assertEquals(0, bookingService.calculateVenueCoversNeeded(80, ServiceType.DELIVERY_WITH_SERVING));
        assertEquals(0, bookingService.calculateVenueCoversNeeded(80, ServiceType.TAKEAWAY));
    }

    @Test
    void testCheckResourceFeasibility_StaffExhaustionThrowsIllegalStateException() {
        LocalDate date = LocalDate.now().plusDays(7);
        // Vendor pool: 5 staff available
        VendorResourcePool pool = new VendorResourcePool(vendor1, 5, 200, 3, 100);
        when(resourcePoolRepository.findByVendorId(10L)).thenReturn(Optional.of(pool));
        when(cateringEventRepository.findActiveEventsForVendorOnDate(eq(10L), eq(date), any())).thenReturn(Collections.emptyList());

        // 60 guests with serving requires 7 staff (exceeds 5)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                bookingService.checkResourceFeasibility(vendor1, date, ServiceType.DELIVERY_WITH_SERVING, 60));

        assertTrue(ex.getMessage().contains("Serving staff capacity exhausted"));
    }

    @Test
    void testCheckResourceFeasibility_UtensilsExhaustionThrowsIllegalStateException() {
        LocalDate date = LocalDate.now().plusDays(7);
        // Vendor pool: 30 utensil sets available
        VendorResourcePool pool = new VendorResourcePool(vendor1, 20, 30, 3, 100);
        when(resourcePoolRepository.findByVendorId(10L)).thenReturn(Optional.of(pool));
        when(cateringEventRepository.findActiveEventsForVendorOnDate(eq(10L), eq(date), any())).thenReturn(Collections.emptyList());

        // 50 guests requires 58 sets (exceeds 30)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                bookingService.checkResourceFeasibility(vendor1, date, ServiceType.DELIVERY_WITH_SERVING, 50));

        assertTrue(ex.getMessage().contains("Utensil sets exhausted"));
    }

    @Test
    void testCheckResourceFeasibility_VehiclesExhaustionThrowsIllegalStateException() {
        LocalDate date = LocalDate.now().plusDays(7);
        // Vendor pool: 1 vehicle available
        VendorResourcePool pool = new VendorResourcePool(vendor1, 20, 200, 1, 100);
        when(resourcePoolRepository.findByVendorId(10L)).thenReturn(Optional.of(pool));

        // Existing event already uses 1 vehicle
        CateringEvent existing = new CateringEvent();
        existing.setVehicleConsumed(1);
        when(cateringEventRepository.findActiveEventsForVendorOnDate(eq(10L), eq(date), any()))
                .thenReturn(List.of(existing));

        // Second delivery request should be rejected
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                bookingService.checkResourceFeasibility(vendor1, date, ServiceType.DELIVERY_ONLY, 20));

        assertTrue(ex.getMessage().contains("Delivery fleet fully booked"));
    }

    @Test
    void testCheckResourceFeasibility_VenueCoversExhaustionThrowsIllegalStateException() {
        LocalDate date = LocalDate.now().plusDays(7);
        // Vendor pool: 50 venue covers
        VendorResourcePool pool = new VendorResourcePool(vendor1, 20, 200, 3, 50);
        when(resourcePoolRepository.findByVendorId(10L)).thenReturn(Optional.of(pool));
        when(cateringEventRepository.findActiveEventsForVendorOnDate(eq(10L), eq(date), any())).thenReturn(Collections.emptyList());

        // 75 guests at venue exceeds 50
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                bookingService.checkResourceFeasibility(vendor1, date, ServiceType.AT_VENUE, 75));

        assertTrue(ex.getMessage().contains("Venue capacity exhausted"));
    }

    @Test
    void testCreateEventBooking_SucceedsWithAccurateResourceSnapshots() {
        LocalDate date = LocalDate.now().plusDays(10);
        VendorResourcePool pool = new VendorResourcePool(vendor1, 15, 200, 4, 120);
        when(resourcePoolRepository.findByVendorId(10L)).thenReturn(Optional.of(pool));
        when(userRepository.findById(100L)).thenReturn(Optional.of(customer));
        when(userRepository.findById(10L)).thenReturn(Optional.of(vendor1));
        when(cateringEventRepository.findActiveEventsForVendorOnDate(eq(10L), eq(date), any())).thenReturn(Collections.emptyList());
        when(cateringEventRepository.save(any(CateringEvent.class))).thenAnswer(i -> {
            CateringEvent e = i.getArgument(0);
            e.setId(99L);
            return e;
        });

        CateringEvent created = bookingService.createEventBooking(
                100L, 10L, null,
                OccasionType.CORPORATE, ServiceType.DELIVERY_WITH_SERVING, ServiceStyle.BUFFET,
                date, "12:00", "15:00", 60,
                "Setup in main conference room", "Tech Park, Building 2", null
        );

        assertNotNull(created);
        assertEquals(BookingStatus.PENDING, created.getStatus());
        assertEquals(7, created.getStaffConsumed()); // 4 servers + 1 captain + 2 kitchen
        assertEquals(69, created.getUtensilsConsumed()); // 60 * 1.15
        assertEquals(1, created.getVehicleConsumed());
        assertEquals(0, created.getVenueCoversConsumed());
        verify(cateringEventRepository, times(1)).save(any(CateringEvent.class));
    }
}
