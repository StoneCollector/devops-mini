package com.cbms.repository;

import com.cbms.model.User;
import com.cbms.model.VendorResourcePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorResourcePoolRepository extends JpaRepository<VendorResourcePool, Long> {
    Optional<VendorResourcePool> findByVendor(User vendor);
    Optional<VendorResourcePool> findByVendorId(Long vendorId);
}
