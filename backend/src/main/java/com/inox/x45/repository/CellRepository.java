package com.inox.x45.repository;

import com.inox.x45.domain.Cell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CellRepository extends JpaRepository<Cell, Long> {
    Optional<Cell> findByCellSerialNumber(String cellSerialNumber);
    Page<Cell> findBySupplierId(Long supplierId, Pageable pageable);
    List<Cell> findByBatchAndLot(String batch, String lot);
}
