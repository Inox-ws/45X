package com.inox.x45.repository;

import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.domain.enums.LinkedEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {
    List<DocumentRecord> findByLinkedEntityTypeAndLinkedEntityId(LinkedEntityType linkedEntityType, Long linkedEntityId);
}
