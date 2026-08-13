package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Material master data synced in from SAP ERP (Section 8). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "material_master", uniqueConstraints = @UniqueConstraint(name = "uk_material_master_code", columnNames = "material_code"))
public class MaterialMaster extends BaseEntity {

    @Column(name = "material_code", nullable = false, length = 50)
    private String materialCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "source", nullable = false, length = 20)
    private String source = "SAP";

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;
}
