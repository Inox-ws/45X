package com.inox.x45.repository;

import com.inox.x45.domain.Supplier;
import com.inox.x45.domain.enums.FeocStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Supplier> findByCountryOfOrigin(String countryOfOrigin, Pageable pageable);
    Page<Supplier> findByFeocStatus(FeocStatus feocStatus, Pageable pageable);
}
