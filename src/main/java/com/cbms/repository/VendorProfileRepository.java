package com.cbms.repository;

import com.cbms.model.User;
import com.cbms.model.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByVendor(User vendor);
    Optional<VendorProfile> findByVendorId(Long vendorId);
}
