package com.inox.x45.repository;

import com.inox.x45.domain.CreditRate;
import com.inox.x45.domain.enums.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CreditRateRepository extends JpaRepository<CreditRate, Long> {
    List<CreditRate> findByComponentTypeOrderByEffectiveFromDesc(ComponentType componentType);

    /** The rate in force for the given component type on the given date, if any (Section 6.1 step 4). */
    @Query("""
        SELECT r FROM CreditRate r
        WHERE r.componentType = :componentType
          AND r.effectiveFrom <= :onDate
          AND (r.effectiveTo IS NULL OR r.effectiveTo >= :onDate)
        ORDER BY r.effectiveFrom DESC
        """)
    List<CreditRate> findEffectiveRates(@Param("componentType") ComponentType componentType, @Param("onDate") LocalDate onDate);

    default Optional<CreditRate> findEffectiveRate(ComponentType componentType, LocalDate onDate) {
        return findEffectiveRates(componentType, onDate).stream().findFirst();
    }
}
