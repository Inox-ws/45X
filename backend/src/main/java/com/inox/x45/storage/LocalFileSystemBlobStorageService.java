package com.inox.x45.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;

/**
 * 'local' profile only: a zero-dependency stand-in for Azure Blob Storage so
 * the whole upload/OCR/download flow works without Azurite or a real storage
 * account (Section 9, Section 15). Blob refs are "containerName/blobName"
 * relative to x45.storage.local.base-dir.
 */
@Service
@Profile("local")
public class LocalFileSystemBlobStorageService implements BlobStorageService {

    private final Path baseDir;

    public LocalFileSystemBlobStorageService(LocalStorageProperties properties) {
        this.baseDir = Path.of(properties.getBaseDir()).toAbsolutePath().normalize();
    }

    @Override
    public String upload(String containerName, String blobName, InputStream content, long contentLength, String contentType) {
        String blobRef = containerName + "/" + blobName;
        Path target = resolve(blobRef);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to write blob " + blobRef, e);
        }
        return blobRef;
    }

    @Override
    public InputStream openStream(String blobRef) {
        try {
            return Files.newInputStream(resolve(blobRef));
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to read blob " + blobRef, e);
        }
    }

    @Override
    public void delete(String blobRef) {
        try {
            Files.deleteIfExists(resolve(blobRef));
        } catch (IOException e) {
            throw new DocumentStorageException("Failed to delete blob " + blobRef, e);
        }
    }

    @Override
    public Optional<String> generateSasUrl(String blobRef, Duration ttl) {
        // No real SAS mechanism for local files - the controller falls back to
        // streaming via openStream() through an authenticated backend endpoint.
        return Optional.empty();
    }

    /** Resolves a blob ref to a path under baseDir, rejecting any attempt to escape it (path traversal). */
    private Path resolve(String blobRef) {
        Path resolved = baseDir.resolve(blobRef).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new DocumentStorageException("Invalid blob reference: " + blobRef);
        }
        return resolved;
    }
}
