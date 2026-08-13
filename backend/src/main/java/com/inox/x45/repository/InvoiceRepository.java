package com.inox.x45.repository;

import com.inox.x45.domain.Invoice;
import com.inox.x45.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    Page<Invoice> findByInvoiceDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Search Invoice (Section 5, module 4): every filter is optional. The
     * supplier filter reaches through the traceability chain
     * (Invoice -> InvoiceModule -> Module -> ModuleCell -> Cell -> Supplier),
     * which is the only place Invoice and Supplier connect at all.
     */
    @Query(value = """
        SELECT DISTINCT i FROM Invoice i
        LEFT JOIN InvoiceModule im ON im.invoice = i
        LEFT JOIN im.module m
        LEFT JOIN ModuleCell mc ON mc.module = m
        LEFT JOIN mc.cell c
        WHERE (:invoiceNumber IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')))
          AND (:customerId IS NULL OR i.customer.id = :customerId)
          AND (:supplierId IS NULL OR c.supplier.id = :supplierId)
          AND (:status IS NULL OR i.status = :status)
          AND (:dateFrom IS NULL OR i.invoiceDate >= :dateFrom)
          AND (:dateTo IS NULL OR i.invoiceDate <= :dateTo)
        """,
        countQuery = """
        SELECT COUNT(DISTINCT i) FROM Invoice i
        LEFT JOIN InvoiceModule im ON im.invoice = i
        LEFT JOIN im.module m
        LEFT JOIN ModuleCell mc ON mc.module = m
        LEFT JOIN mc.cell c
        WHERE (:invoiceNumber IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')))
          AND (:customerId IS NULL OR i.customer.id = :customerId)
          AND (:supplierId IS NULL OR c.supplier.id = :supplierId)
          AND (:status IS NULL OR i.status = :status)
          AND (:dateFrom IS NULL OR i.invoiceDate >= :dateFrom)
          AND (:dateTo IS NULL OR i.invoiceDate <= :dateTo)
        """)
    Page<Invoice> search(
        @Param("invoiceNumber") String invoiceNumber,
        @Param("customerId") Long customerId,
        @Param("supplierId") Long supplierId,
        @Param("status") InvoiceStatus status,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable);
}
