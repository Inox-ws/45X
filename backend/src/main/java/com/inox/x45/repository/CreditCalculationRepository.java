package com.inox.x45.repository;

import com.inox.x45.domain.CreditCalculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CreditCalculationRepository extends JpaRepository<CreditCalculation, Long> {
    List<CreditCalculation> findByPeriod(LocalDate period);
    Page<CreditCalculation> findByCustomerId(Long customerId, Pageable pageable);
    Page<CreditCalculation> findBySupplierId(Long supplierId, Pageable pageable);
    Page<CreditCalculation> findByPeriodBetween(LocalDate from, LocalDate to, Pageable pageable);
}
