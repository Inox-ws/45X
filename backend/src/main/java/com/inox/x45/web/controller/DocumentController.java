package com.inox.x45.web.controller;

import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.repository.DocumentRecordRepository;
import com.inox.x45.storage.BlobStorageService;
import com.inox.x45.web.dto.DownloadUrlResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Document download (Section 9). /download-url returns a short-lived SAS URL
 * when the storage backend supports one (azure profile); the frontend falls
 * back to streaming through /raw (used by the local profile, which has no
 * SAS mechanism) when it doesn't.
 *
 * TODO(Milestone 9 hardening): restrict downloads to users with a legitimate
 * relationship to the linked entity, rather than any authenticated user.
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private static final Duration SAS_TTL = Duration.ofMinutes(15);

    private final DocumentRecordRepository documentRecordRepository;
    private final BlobStorageService blobStorageService;

    public DocumentController(DocumentRecordRepository documentRecordRepository, BlobStorageService blobStorageService) {
        this.documentRecordRepository = documentRecordRepository;
        this.blobStorageService = blobStorageService;
    }

    @GetMapping("/{id}/download-url")
    public DownloadUrlResponse downloadUrl(@PathVariable Long id) {
        DocumentRecord document = getOrThrow(id);
        return new DownloadUrlResponse(blobStorageService.generateSasUrl(document.getBlobRef(), SAS_TTL).orElse(null));
    }

    @GetMapping("/{id}/raw")
    public ResponseEntity<InputStreamResource> raw(@PathVariable Long id) {
        DocumentRecord document = getOrThrow(id);
        InputStreamResource body = new InputStreamResource(blobStorageService.openStream(document.getBlobRef()));
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(document.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(document.getFileName()).build().toString())
            .body(body);
    }

    private DocumentRecord getOrThrow(Long id) {
        return documentRecordRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
    }
}
