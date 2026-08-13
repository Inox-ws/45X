package com.inox.x45.web.dto;

import com.inox.x45.ocr.ExtractedInvoiceData;

public record UploadInvoiceResponse(Long documentId, String fileName, ExtractedInvoiceData extracted) {}
