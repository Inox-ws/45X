package com.inox.x45.domain;

import com.inox.x45.domain.enums.FeocListEntryType;
import com.inox.x45.domain.enums.FeocListStatus;
import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Admin-maintained FEOC/PFE country or entity list (Section 6.2). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feoc_list_entry", uniqueConstraints = @UniqueConstraint(name = "uk_feoc_list_entry", columnNames = {"entry_type", "name"}))
public class FeocListEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private FeocListEntryType entryType;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeocListStatus status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;
}
