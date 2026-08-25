package com.cbms.repository;

import com.cbms.model.Booking;
import com.cbms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findBySlotVendorOrderByCreatedAtDesc(User vendor);
    List<Booking> findBySlotVendorIdOrderByCreatedAtDesc(Long vendorId);
    List<Booking> findBySlotId(Long slotId);
    List<Booking> findAllByOrderByCreatedAtDesc();
}

