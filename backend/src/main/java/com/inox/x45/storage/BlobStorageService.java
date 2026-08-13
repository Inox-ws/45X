package com.inox.x45.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

/**
 * Abstraction over document storage (Section 9). Only a blob reference and
 * metadata are ever stored in SQL - the file bytes always live behind this
 * interface, never in the database.
 *
 * 'local' profile: LocalFileSystemBlobStorageService (files on local disk).
 * 'azure' profile: AzureBlobStorageService (real Azure Blob Storage, Managed Identity).
 */
public interface BlobStorageService {

    /**
     * @return an opaque blob reference (container/blobName) to persist on the DocumentRecord.
     */
    String upload(String containerName, String blobName, InputStream content, long contentLength, String contentType);

    /** Reads the blob's bytes back. Used by the local-dev raw-download fallback. */
    InputStream openStream(String blobRef);

    void delete(String blobRef);

    /**
     * A short-lived, pre-authenticated download URL the client can be redirected
     * to directly (Section 9 - "never expose the storage key to the client").
     * Empty when the implementation has no such mechanism (local dev), in which
     * case the caller should fall back to streaming via openStream() instead.
     */
    Optional<String> generateSasUrl(String blobRef, Duration ttl);
}
