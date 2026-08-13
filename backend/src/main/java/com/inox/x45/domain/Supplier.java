package com.inox.x45.domain;

import com.inox.x45.domain.enums.FeocStatus;
import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "supplier")
public class Supplier extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "country_of_origin", nullable = false, length = 100)
    private String countryOfOrigin;

    @Enumerated(EnumType.STRING)
    @Column(name = "feoc_status", nullable = false, length = 20)
    private FeocStatus feocStatus = FeocStatus.NEEDS_REVIEW;

    @Column(name = "feoc_notes", length = 1000)
    private String feocNotes;

    @Column(name = "material_info", length = 1000)
    private String materialInfo;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
