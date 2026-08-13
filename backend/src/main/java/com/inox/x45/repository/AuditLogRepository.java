package com.inox.x45.repository;

import com.inox.x45.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);
    Page<AuditLog> findByActorEmail(String actorEmail, Pageable pageable);
}
