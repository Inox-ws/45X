package com.inox.x45.repository;

import com.inox.x45.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByModuleSerialNumber(String moduleSerialNumber);
}
