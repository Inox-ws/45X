package com.inox.x45.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/** File upload validation shared by every upload endpoint (Section 9, Section 11). */
@Service
public class FileValidationService {

    private final DocumentUploadProperties uploadProperties;

    public FileValidationService(DocumentUploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    /**
     * @return the detected file type once every check passes.
     * @throws IllegalArgumentException with a user-facing message on any validation failure
     *         (mapped to 400 by GlobalExceptionHandler).
     */
    public AllowedFileType validate(MultipartFile file, Set<AllowedFileType> allowedTypes) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }
        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new IllegalArgumentException(
                "File exceeds the maximum allowed size of " + uploadProperties.getMaxSizeMb() + " MB.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Uploaded file has no name.");
        }

        AllowedFileType declaredType = AllowedFileType.forFileName(fileName)
            .filter(allowedTypes::contains)
            .orElseThrow(() -> new IllegalArgumentException(
                "Unsupported file type. Allowed: " + allowedTypes.stream().map(AllowedFileType::extension).toList()));

        byte[] header;
        try {
            header = file.getInputStream().readNBytes(8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file.");
        }

        if (!declaredType.matchesMagicBytes(header)) {
            throw new IllegalArgumentException(
                "File content does not match its " + declaredType.extension() + " extension.");
        }

        return declaredType;
    }
}
