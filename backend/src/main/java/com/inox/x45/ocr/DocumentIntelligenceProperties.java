package com.inox.x45.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 'azure' profile only (Section 9). PLACEHOLDER - set per environment. */
@Component
@ConfigurationProperties(prefix = "x45.ocr.document-intelligence")
public class DocumentIntelligenceProperties {

    /** e.g. https://x45-doc-intelligence.cognitiveservices.azure.com */
    private String endpoint;

    private String apiVersion = "2024-11-30";

    private long pollTimeoutSeconds = 60;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public long getPollTimeoutSeconds() {
        return pollTimeoutSeconds;
    }

    public void setPollTimeoutSeconds(long pollTimeoutSeconds) {
        this.pollTimeoutSeconds = pollTimeoutSeconds;
    }
}
