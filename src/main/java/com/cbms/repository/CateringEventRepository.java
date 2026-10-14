package com.cbms.repository;

import com.cbms.model.BookingStatus;
import com.cbms.model.CateringEvent;
import com.cbms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CateringEventRepository extends JpaRepository<CateringEvent, Long> {

    List<CateringEvent> findByCustomerOrderByEventDateDescCreatedAtDesc(User customer);

    List<CateringEvent> findByCustomerIdOrderByEventDateDescCreatedAtDesc(Long customerId);

    List<CateringEvent> findByVendorOrderByEventDateDescCreatedAtDesc(User vendor);

    List<CateringEvent> findByVendorIdOrderByEventDateDescCreatedAtDesc(Long vendorId);

    List<CateringEvent> findAllByOrderByEventDateDescCreatedAtDesc();

    @Query("SELECT e FROM CateringEvent e WHERE e.vendor.id = :vendorId AND e.eventDate = :eventDate AND e.status != :excludedStatus")
    List<CateringEvent> findActiveEventsForVendorOnDate(@Param("vendorId") Long vendorId,
                                                       @Param("eventDate") LocalDate eventDate,
                                                       @Param("excludedStatus") BookingStatus excludedStatus);
}
