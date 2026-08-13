package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Raw production/machine-line data synced in from MES (Section 8), linked to
 * whichever cell or module it was recorded against. Exactly one of cell/module
 * should be set - enforced in the MES sync service, not the schema, since
 * cross-column conditional constraints are awkward to express portably.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "production_data_record")
public class ProductionDataRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    @Column(name = "batch_lot", length = 50)
    private String batchLot;

    @Column(name = "machine_id", length = 50)
    private String machineId;

    @Column(name = "line_id", length = 50)
    private String lineId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Lob
    @Column(name = "raw_payload")
    private String rawPayload;
}
