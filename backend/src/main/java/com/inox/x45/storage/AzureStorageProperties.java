package com.inox.x45.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 'azure' profile only (Section 9, Section 15). PLACEHOLDER - set per environment. */
@Component
@ConfigurationProperties(prefix = "x45.storage.azure")
public class AzureStorageProperties {

    /** e.g. https://x45portalstorage.blob.core.windows.net */
    private String accountUrl;

    private long sasTtlMinutes = 15;

    public String getAccountUrl() {
        return accountUrl;
    }

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }

    public long getSasTtlMinutes() {
        return sasTtlMinutes;
    }

    public void setSasTtlMinutes(long sasTtlMinutes) {
        this.sasTtlMinutes = sasTtlMinutes;
    }
}
