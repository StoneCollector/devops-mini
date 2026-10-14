package com.cbms.repository;

import com.cbms.model.User;
import com.cbms.model.VendorCuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorCuisineRepository extends JpaRepository<VendorCuisine, Long> {
    List<VendorCuisine> findByVendorAndActiveTrue(User vendor);
    List<VendorCuisine> findByVendorOrderByCuisineNameAsc(User vendor);
    List<VendorCuisine> findByVendorIdOrderByCuisineNameAsc(Long vendorId);
    List<VendorCuisine> findByActiveTrueOrderByCuisineNameAsc();
}
