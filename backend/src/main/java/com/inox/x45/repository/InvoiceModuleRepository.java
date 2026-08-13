package com.inox.x45.repository;

import com.inox.x45.domain.InvoiceModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceModuleRepository extends JpaRepository<InvoiceModule, Long> {
    List<InvoiceModule> findByInvoiceId(Long invoiceId);
    List<InvoiceModule> findByModuleId(Long moduleId);
}
