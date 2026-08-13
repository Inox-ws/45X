package com.inox.x45.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 'local' profile only: where the filesystem-backed blob storage fallback keeps its files. */
@Component
@ConfigurationProperties(prefix = "x45.storage.local")
public class LocalStorageProperties {

    private String baseDir = "./data/blob-storage";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
