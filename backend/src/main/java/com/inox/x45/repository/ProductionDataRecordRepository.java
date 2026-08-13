package com.inox.x45.repository;

import com.inox.x45.domain.ProductionDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductionDataRecordRepository extends JpaRepository<ProductionDataRecord, Long> {
    List<ProductionDataRecord> findByCellId(Long cellId);
    List<ProductionDataRecord> findByModuleId(Long moduleId);
}
