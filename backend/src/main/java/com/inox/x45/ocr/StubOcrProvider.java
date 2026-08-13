package com.inox.x45.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 'local' profile only: returns fixed, clearly-fake extracted data so the
 * Upload Invoice review/confirm screen has something to work with in local
 * dev and demos, without any external OCR dependency (Section 9).
 */
@Service
@Profile("local")
public class StubOcrProvider implements OcrProvider {

    @Override
    public ExtractedInvoiceData extractInvoiceData(InputStream content, String contentType) {
        String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(10_000, 99_999));
        return new ExtractedInvoiceData(
            "OCR-STUB-" + suffix,
            LocalDate.now(),
            "Demo Customer (edit me - OCR stub, Milestone 4 local profile)",
            new BigDecimal("1000.00"),
            "USD",
            List.of(new ExtractedLineItem("Solar module (sample line item)",
                BigDecimal.TEN, new BigDecimal("100.00"), new BigDecimal("1000.00"), new BigDecimal("5400"))),
            new BigDecimal("5400")
        );
    }
}
