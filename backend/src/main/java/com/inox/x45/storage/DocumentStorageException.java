package com.inox.x45.storage;

/**
 * Deliberately not named BlobStorageException - that name collides with
 * com.azure.storage.blob.models.BlobStorageException from the Azure SDK,
 * which this package's 'azure' profile implementation legitimately needs to
 * reference.
 */
public class DocumentStorageException extends RuntimeException {
    public DocumentStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentStorageException(String message) {
        super(message);
    }
}
