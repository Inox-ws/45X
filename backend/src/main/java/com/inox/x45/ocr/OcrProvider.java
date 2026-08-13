package com.inox.x45.ocr;

import java.io.InputStream;

/**
 * Abstracts the OCR/data-extraction backend (Section 9).
 * 'local' profile: StubOcrProvider (fixed mock data, no external dependency).
 * 'azure' profile: AzureDocumentIntelligenceOcrProvider (Azure AI Document Intelligence).
 */
public interface OcrProvider {
    ExtractedInvoiceData extractInvoiceData(InputStream content, String contentType);
}
