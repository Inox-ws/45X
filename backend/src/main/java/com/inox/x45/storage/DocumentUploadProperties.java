package com.inox.x45.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Shared upload limits, regardless of active profile (Section 9, Section 11). */
@Component
@ConfigurationProperties(prefix = "x45.storage.upload")
public class DocumentUploadProperties {

    private long maxSizeMb = 25;

    public long getMaxSizeMb() {
        return maxSizeMb;
    }

    public void setMaxSizeMb(long maxSizeMb) {
        this.maxSizeMb = maxSizeMb;
    }

    public long getMaxSizeBytes() {
        return maxSizeMb * 1024 * 1024;
    }
}
