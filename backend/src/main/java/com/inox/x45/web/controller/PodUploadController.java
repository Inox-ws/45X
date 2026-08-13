package com.inox.x45.web.controller;

import com.inox.x45.audit.AuditService;
import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.domain.enums.DocumentType;
import com.inox.x45.domain.enums.LinkedEntityType;
import com.inox.x45.repository.InvoiceRepository;
import com.inox.x45.service.DocumentService;
import com.inox.x45.storage.AllowedFileType;
import com.inox.x45.web.dto.PodUploadResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/** Upload POD (Section 5, module 3): links a proof-of-delivery/shipping document straight to an existing invoice. */
@RestController
@RequestMapping("/api/v1/pod")
public class PodUploadController {

    private static final Set<AllowedFileType> ALLOWED_TYPES = Set.of(AllowedFileType.PDF, AllowedFileType.PNG, AllowedFileType.JPG);

    private final DocumentService documentService;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;

    public PodUploadController(DocumentService documentService, InvoiceRepository invoiceRepository, AuditService auditService) {
        this.documentService = documentService;
        this.invoiceRepository = invoiceRepository;
        this.auditService = auditService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('LOGISTICS','ADMIN')")
    public PodUploadResponse upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam("invoiceId") Long invoiceId,
                                     Authentication authentication) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceId);
        }

        DocumentRecord document = documentService.upload(
            file, DocumentType.POD, ALLOWED_TYPES, LinkedEntityType.INVOICE, invoiceId, authentication);

        PodUploadResponse response = new PodUploadResponse(document.getId(), document.getFileName(), invoiceId);
        auditService.record(authentication, "UPLOAD", "DocumentRecord", document.getId(), null, response);
        return response;
    }
}
