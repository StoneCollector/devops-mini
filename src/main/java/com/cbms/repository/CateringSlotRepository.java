package com.cbms.repository;

import com.cbms.model.CateringSlot;
import com.cbms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CateringSlotRepository extends JpaRepository<CateringSlot, Long> {
    List<CateringSlot> findByDate(LocalDate date);
    List<CateringSlot> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
    List<CateringSlot> findByVendorOrderByDateAsc(User vendor);
    List<CateringSlot> findByVendorIdOrderByDateAsc(Long vendorId);
    List<CateringSlot> findByDateGreaterThanEqualAndMenuTypeContainingIgnoreCaseOrderByDateAsc(LocalDate date, String menuType);
    List<CateringSlot> findByDateAndMenuTypeContainingIgnoreCase(LocalDate date, String menuType);
    List<CateringSlot> findByMenuTypeContainingIgnoreCase(String menuType);
}


