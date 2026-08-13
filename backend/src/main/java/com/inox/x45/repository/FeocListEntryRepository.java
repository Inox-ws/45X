package com.inox.x45.repository;

import com.inox.x45.domain.FeocListEntry;
import com.inox.x45.domain.enums.FeocListEntryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeocListEntryRepository extends JpaRepository<FeocListEntry, Long> {
    Optional<FeocListEntry> findByEntryTypeAndNameIgnoreCase(FeocListEntryType entryType, String name);
}
