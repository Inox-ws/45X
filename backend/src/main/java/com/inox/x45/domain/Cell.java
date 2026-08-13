package com.inox.x45.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A solar cell tracked from MES cell-serial-number generation (Section 6.3). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cell", uniqueConstraints = @UniqueConstraint(name = "uk_cell_serial_number", columnNames = "cell_serial_number"))
public class Cell extends BaseEntity {

    @Column(name = "cell_serial_number", nullable = false, length = 100)
    private String cellSerialNumber;

    @Column(name = "batch", length = 50)
    private String batch;

    @Column(name = "lot", length = 50)
    private String lot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "wattage", nullable = false, precision = 10, scale = 3)
    private BigDecimal wattage;

    @Column(name = "country_of_origin", nullable = false, length = 100)
    private String countryOfOrigin;

    @Enumerated(EnumType.STRING)
    @Column(name = "feoc_status", nullable = false, length = 20)
    private FeocStatus feocStatus = FeocStatus.NEEDS_REVIEW;

    @Column(name = "received_date")
    private LocalDate receivedDate;
}
