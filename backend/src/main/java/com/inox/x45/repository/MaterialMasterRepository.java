package com.inox.x45.repository;

import com.inox.x45.domain.MaterialMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialMasterRepository extends JpaRepository<MaterialMaster, Long> {
    Optional<MaterialMaster> findByMaterialCode(String materialCode);
}
