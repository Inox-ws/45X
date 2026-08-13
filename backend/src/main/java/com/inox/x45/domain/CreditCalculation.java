package com.inox.x45.domain;

import com.inox.x45.domain.enums.ComponentType;
import com.inox.x45.domain.enums.FeocStatus;
import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** One run of the credit calculation engine (Section 6.1) for a period/supplier/customer/component slice. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "credit_calculation")
public class CreditCalculation extends BaseEntity {

    /** First day of the calculation month, e.g. 2026-07-01 for the July 2026 period. */
    @Column(name = "period", nullable = false)
    private LocalDate period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 30)
    private ComponentType componentType;

    @Column(name = "eligible_wattage", nullable = false, precision = 18, scale = 3)
    private BigDecimal eligibleWattage;

    @Column(name = "rate_per_watt", nullable = false, precision = 10, scale = 6)
    private BigDecimal ratePerWatt;

    @Column(name = "credit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "feoc_status", nullable = false, length = 20)
    private FeocStatus feocStatus;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculated_by_user_id")
    private AppUser calculatedBy;
}
