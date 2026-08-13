package com.inox.x45.domain;

import com.inox.x45.domain.support.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Which cells were built into which module - the Cell -> Module link in the traceability chain. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "module_cell", uniqueConstraints = {
    @UniqueConstraint(name = "uk_module_cell_module_cell", columnNames = {"module_id", "cell_id"}),
    @UniqueConstraint(name = "uk_module_cell_cell", columnNames = "cell_id")
})
public class ModuleCell extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cell_id", nullable = false)
    private Cell cell;
}
