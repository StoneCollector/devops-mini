package com.cbms.repository;

import com.cbms.model.CateringSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CateringSlotRepository extends JpaRepository<CateringSlot, Long> {
    List<CateringSlot> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
}
