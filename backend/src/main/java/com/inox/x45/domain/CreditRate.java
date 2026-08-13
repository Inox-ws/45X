package com.inox.x45.domain;

import com.inox.x45.domain.enums.ComponentType;
import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Admin-configurable 45X credit rate per component type, effective-dated
 * (Section 6.1). Values must be verified against current IRS/Treasury
 * guidance - the engine only ever reads rates from this table, never a
 * hard-coded constant.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "credit_rate")
public class CreditRate extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 30)
    private ComponentType componentType;

    @Column(name = "rate_per_watt", nullable = false, precision = 10, scale = 6)
    private BigDecimal ratePerWatt;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
