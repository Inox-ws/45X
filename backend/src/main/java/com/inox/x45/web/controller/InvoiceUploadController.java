package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.Customer;
import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.domain.Invoice;
import com.inox.x45.domain.enums.DocumentType;
import com.inox.x45.domain.enums.InvoiceSource;
import com.inox.x45.domain.enums.InvoiceStatus;
import com.inox.x45.domain.enums.LinkedEntityType;
import com.inox.x45.ocr.ExtractedInvoiceData;
import com.inox.x45.ocr.OcrProvider;
import com.inox.x45.repository.CustomerRepository;
import com.inox.x45.repository.DocumentRecordRepository;
import com.inox.x45.repository.InvoiceRepository;
import com.inox.x45.service.DocumentService;
import com.inox.x45.storage.AllowedFileType;
import com.inox.x45.web.dto.ConfirmInvoiceRequest;
import com.inox.x45.web.dto.InvoiceResponse;
import com.inox.x45.web.dto.UploadInvoiceResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

/**
 * Upload Invoice (Section 5, module 2): upload -> OCR pre-fill -> the user
 * confirms/edits the extracted fields -> the Invoice record is created.
 */
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceUploadController {

    private static final Set<AllowedFileType> ALLOWED_TYPES = Set.of(AllowedFileType.PDF, AllowedFileType.XLSX);

    private final DocumentService documentService;
    private final OcrProvider ocrProvider;
    private final DocumentRecordRepository documentRecordRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public InvoiceUploadController(DocumentService documentService, OcrProvider ocrProvider,
                                    DocumentRecordRepository documentRecordRepository,
                                    InvoiceRepository invoiceRepository, CustomerRepository customerRepository,
                                    AuditService auditService) {
        this.documentService = documentService;
        this.ocrProvider = ocrProvider;
        this.documentRecordRepository = documentRecordRepository;
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public UploadInvoiceResponse upload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        DocumentRecord document = documentService.upload(
            file, DocumentType.INVOICE, ALLOWED_TYPES, null, null, authentication);

        ExtractedInvoiceData extracted;
        try {
            extracted = ocrProvider.extractInvoiceData(
                new ByteArrayInputStream(file.getBytes()), document.getContentType());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file for OCR", e);
        }

        return new UploadInvoiceResponse(document.getId(), document.getFileName(), extracted);
    }

    @PostMapping("/{documentId}/confirm")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    @Transactional
    public InvoiceResponse confirm(@PathVariable Long documentId, @Valid @RequestBody ConfirmInvoiceRequest request,
                                    Authentication authentication) {
        DocumentRecord document = documentRecordRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        if (document.getDocumentType() != DocumentType.INVOICE) {
            throw new IllegalArgumentException("Document " + documentId + " was not uploaded as an invoice.");
        }
        if (document.getLinkedEntityId() != null) {
            throw new IllegalArgumentException("Document " + documentId + " has already been confirmed as an invoice.");
        }
        if (invoiceRepository.findByInvoiceNumber(request.invoiceNumber()).isPresent()) {
            throw new IllegalArgumentException("Invoice number already exists: " + request.invoiceNumber());
        }
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.invoiceNumber());
        invoice.setCustomer(customer);
        invoice.setInvoiceDate(request.invoiceDate());
        invoice.setAmount(request.amount());
        invoice.setCurrency(request.currency());
        invoice.setStatus(InvoiceStatus.PENDING_VALIDATION);
        invoice.setSource(InvoiceSource.MANUAL);
        invoice.setBlobRef(document.getBlobRef());
        invoice = invoiceRepository.save(invoice);

        document.setLinkedEntityType(LinkedEntityType.INVOICE);
        document.setLinkedEntityId(invoice.getId());
        documentRecordRepository.save(document);

        InvoiceResponse response = toResponse(invoice, document.getId());
        auditService.record(authentication, "CREATE", "Invoice", invoice.getId(), null, response);
        return response;
    }

    private InvoiceResponse toResponse(Invoice invoice, Long documentId) {
        return new InvoiceResponse(
            invoice.getId(), invoice.getInvoiceNumber(), invoice.getCustomer().getId(), invoice.getCustomer().getName(),
            invoice.getInvoiceDate(), invoice.getAmount(), invoice.getCurrency(), invoice.getStatus().name(), documentId);
    }
}
