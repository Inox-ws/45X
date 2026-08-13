package com.inox.x45.repository;

import com.inox.x45.domain.ModuleCell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleCellRepository extends JpaRepository<ModuleCell, Long> {
    List<ModuleCell> findByModuleId(Long moduleId);
    Optional<ModuleCell> findByCellId(Long cellId);
}
