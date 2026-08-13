package com.inox.x45.storage;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.models.UserDelegationKey;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 'azure' profile: real Azure Blob Storage, authenticated via Managed Identity
 * (Section 9, Section 15) - no account key or connection string is ever used.
 * SAS generation uses a user delegation key (obtained via the same Managed
 * Identity credential) rather than an account key, per Section 9's
 * requirement to never expose the storage key to the client.
 *
 * The exact SAS/user-delegation-key API shape here is best-effort against the
 * azure-storage-blob SDK - verify against current SDK docs the first time this
 * runs against a real storage account, since this hasn't been compiled here
 * (see the Milestone 1 note on this sandbox's offline Maven).
 */
@Service
@Profile("azure")
public class AzureBlobStorageService implements BlobStorageService {

    private final AzureStorageProperties properties;
    private BlobServiceClient blobServiceClient;

    public AzureBlobStorageService(AzureStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        this.blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(properties.getAccountUrl())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    @Override
    public String upload(String containerName, String blobName, InputStream content, long contentLength, String contentType) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        if (!container.exists()) {
            container.create();
        }
        BlobClient blobClient = container.getBlobClient(blobName);
        blobClient.upload(content, contentLength, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        return containerName + "/" + blobName;
    }

    @Override
    public InputStream openStream(String blobRef) {
        BlobClient blobClient = blobClientFor(blobRef);
        try {
            return blobClient.openInputStream();
        } catch (RuntimeException e) {
            throw new DocumentStorageException("Failed to open blob " + blobRef, e);
        }
    }

    @Override
    public void delete(String blobRef) {
        blobClientFor(blobRef).deleteIfExists();
    }

    @Override
    public Optional<String> generateSasUrl(String blobRef, Duration ttl) {
        BlobClient blobClient = blobClientFor(blobRef);
        OffsetDateTime start = OffsetDateTime.now().minusMinutes(1); // small clock-skew allowance
        OffsetDateTime expiry = OffsetDateTime.now().plus(ttl);

        UserDelegationKey delegationKey = blobServiceClient.getUserDelegationKey(start, expiry);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiry, permission)
            .setStartTime(start);

        String sasToken = blobClient.generateUserDelegationSas(sasValues, delegationKey);
        return Optional.of(blobClient.getBlobUrl() + "?" + sasToken);
    }

    private BlobClient blobClientFor(String blobRef) {
        int separator = blobRef.indexOf('/');
        if (separator < 0) {
            throw new DocumentStorageException("Invalid blob reference: " + blobRef);
        }
        String containerName = blobRef.substring(0, separator);
        String blobName = blobRef.substring(separator + 1);
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);
    }
}
