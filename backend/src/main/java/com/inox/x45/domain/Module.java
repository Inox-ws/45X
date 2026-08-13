package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A solar module assembled from one or more cells (see ModuleCell), Section 6.3. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "module", uniqueConstraints = @UniqueConstraint(name = "uk_module_serial_number", columnNames = "module_serial_number"))
public class Module extends BaseEntity {

    @Column(name = "module_serial_number", nullable = false, length = 100)
    private String moduleSerialNumber;

    @Column(name = "wattage", nullable = false, precision = 10, scale = 3)
    private BigDecimal wattage;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "production_line", length = 50)
    private String productionLine;

    @Column(name = "machine_id", length = 50)
    private String machineId;
}
