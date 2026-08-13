package com.inox.x45.web.controller;

import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.domain.Invoice;
import com.inox.x45.domain.enums.InvoiceStatus;
import com.inox.x45.domain.enums.LinkedEntityType;
import com.inox.x45.repository.DocumentRecordRepository;
import com.inox.x45.repository.InvoiceRepository;
import com.inox.x45.web.dto.DocumentSummaryResponse;
import com.inox.x45.web.dto.InvoiceDetailResponse;
import com.inox.x45.web.dto.InvoiceListItemResponse;
import com.inox.x45.web.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Search Invoice (Section 5, module 4). */
@RestController
@RequestMapping("/api/v1/invoices")
@PreAuthorize("hasAnyRole('FINANCE','MANAGEMENT','ADMIN')")
public class InvoiceSearchController {

    private final InvoiceRepository invoiceRepository;
    private final DocumentRecordRepository documentRecordRepository;

    public InvoiceSearchController(InvoiceRepository invoiceRepository, DocumentRecordRepository documentRecordRepository) {
        this.invoiceRepository = invoiceRepository;
        this.documentRecordRepository = documentRecordRepository;
    }

    @GetMapping
    public PageResponse<InvoiceListItemResponse> search(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Invoice> result = invoiceRepository.search(invoiceNumber, customerId, supplierId, status, dateFrom, dateTo,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "invoiceDate")));
        return PageResponse.of(result.map(this::toListItem));
    }

    @GetMapping("/{id}")
    public InvoiceDetailResponse detail(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
        return toDetail(invoice);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> export(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        List<Invoice> invoices = invoiceRepository.search(invoiceNumber, customerId, supplierId, status, dateFrom, dateTo,
            PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "invoiceDate"))).getContent();

        StringBuilder csv = new StringBuilder("Invoice Number,Customer,Invoice Date,Amount,Currency,Status\n");
        for (Invoice invoice : invoices) {
            csv.append(csvEscape(invoice.getInvoiceNumber())).append(',')
                .append(csvEscape(invoice.getCustomer().getName())).append(',')
                .append(invoice.getInvoiceDate()).append(',')
                .append(invoice.getAmount()).append(',')
                .append(invoice.getCurrency()).append(',')
                .append(invoice.getStatus()).append('\n');
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoices.csv")
            .body(csv.toString());
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") || value.contains("\"") ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    private InvoiceListItemResponse toListItem(Invoice invoice) {
        return new InvoiceListItemResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getCustomer().getName(),
            invoice.getInvoiceDate(), invoice.getAmount(), invoice.getCurrency(), invoice.getStatus().name());
    }

    private InvoiceDetailResponse toDetail(Invoice invoice) {
        List<DocumentSummaryResponse> documents = documentRecordRepository
            .findByLinkedEntityTypeAndLinkedEntityId(LinkedEntityType.INVOICE, invoice.getId()).stream()
            .map(this::toDocumentSummary)
            .toList();
        return new InvoiceDetailResponse(invoice.getId(), invoice.getInvoiceNumber(), invoice.getCustomer().getId(),
            invoice.getCustomer().getName(), invoice.getInvoiceDate(), invoice.getAmount(), invoice.getCurrency(),
            invoice.getStatus().name(), invoice.getSource().name(), documents);
    }

    private DocumentSummaryResponse toDocumentSummary(DocumentRecord document) {
        return new DocumentSummaryResponse(document.getId(), document.getDocumentType().name(),
            document.getFileName(), document.getUploadedAt());
    }
}
