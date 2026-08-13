package com.inox.x45.storage;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The four file types Section 9 allows, each with its own magic-byte
 * signature so an upload's actual content can be checked against what its
 * extension/content-type claims (a lightweight content-type sniffing check,
 * not a substitute for real antivirus scanning - see Section 9's separate
 * "virus/content-type check" note; malware scanning itself is a production
 * concern to wire in at the infra level, e.g. Defender for Storage).
 */
public enum AllowedFileType {
    PDF(".pdf", "application/pdf", new byte[] {0x25, 0x50, 0x44, 0x46}),
    PNG(".png", "image/png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}),
    JPG(".jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
    // .xlsx is a zip container - PK magic only confirms "it's a zip", not specifically an xlsx,
    // which is an acceptable tradeoff for a lightweight sniff check.
    XLSX(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        new byte[] {0x50, 0x4B, 0x03, 0x04});

    private final String extension;
    private final String contentType;
    private final byte[] magicBytes;

    AllowedFileType(String extension, String contentType, byte[] magicBytes) {
        this.extension = extension;
        this.contentType = contentType;
        this.magicBytes = magicBytes;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    public boolean matchesMagicBytes(byte[] header) {
        if (header.length < magicBytes.length) {
            return false;
        }
        for (int i = 0; i < magicBytes.length; i++) {
            if (header[i] != magicBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public static Optional<AllowedFileType> forFileName(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return List.of(values()).stream().filter(type -> lower.endsWith(type.extension)).findFirst();
    }
}
