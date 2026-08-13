package com.inox.x45.service;

import com.inox.x45.domain.AppUser;
import com.inox.x45.domain.DocumentRecord;
import com.inox.x45.domain.enums.DocumentType;
import com.inox.x45.domain.enums.LinkedEntityType;
import com.inox.x45.repository.AppUserRepository;
import com.inox.x45.repository.DocumentRecordRepository;
import com.inox.x45.security.CurrentUserResolver;
import com.inox.x45.storage.AllowedFileType;
import com.inox.x45.storage.BlobStorageService;
import com.inox.x45.storage.FileValidationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Uploads a file to blob storage and records its metadata (Section 9). */
@Service
public class DocumentService {

    private final FileValidationService fileValidationService;
    private final BlobStorageService blobStorageService;
    private final DocumentRecordRepository documentRecordRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentUserResolver currentUserResolver;

    public DocumentService(FileValidationService fileValidationService,
                            BlobStorageService blobStorageService,
                            DocumentRecordRepository documentRecordRepository,
                            AppUserRepository appUserRepository,
                            CurrentUserResolver currentUserResolver) {
        this.fileValidationService = fileValidationService;
        this.blobStorageService = blobStorageService;
        this.documentRecordRepository = documentRecordRepository;
        this.appUserRepository = appUserRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional
    public DocumentRecord upload(MultipartFile file, DocumentType documentType, Set<AllowedFileType> allowedTypes,
                                  LinkedEntityType linkedEntityType, Long linkedEntityId, Authentication uploadedBy) {
        AllowedFileType detectedType = fileValidationService.validate(file, allowedTypes);

        String blobName = UUID.randomUUID() + detectedType.extension();
        String containerName = containerFor(documentType);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }

        String blobRef = blobStorageService.upload(containerName, blobName,
            new ByteArrayInputStream(bytes), bytes.length, detectedType.contentType());

        DocumentRecord record = new DocumentRecord();
        record.setDocumentType(documentType);
        record.setBlobRef(blobRef);
        record.setFileName(file.getOriginalFilename());
        record.setContentType(detectedType.contentType());
        record.setSizeBytes(bytes.length);
        record.setLinkedEntityType(linkedEntityType);
        record.setLinkedEntityId(linkedEntityId);
        record.setUploadedAt(Instant.now());
        // Best-effort: only resolves for the 'local' profile's seeded demo user today.
        // Entra ID users get an AppUser row provisioned in Milestone 5 (User Management);
        // until then this is left null rather than guessed at.
        resolveUploader(uploadedBy).ifPresent(record::setUploadedBy);
        return documentRecordRepository.save(record);
    }

    private String containerFor(DocumentType documentType) {
        return switch (documentType) {
            case INVOICE -> "invoices";
            case POD -> "pods";
            case PACKING_LIST -> "packing-lists";
            case BILL_OF_LADING -> "bills-of-lading";
            case OTHER -> "other";
        };
    }

    private java.util.Optional<AppUser> resolveUploader(Authentication authentication) {
        String email = currentUserResolver.resolve(authentication).email();
        return email == null ? java.util.Optional.empty() : appUserRepository.findByEmailIgnoreCase(email);
    }
}
