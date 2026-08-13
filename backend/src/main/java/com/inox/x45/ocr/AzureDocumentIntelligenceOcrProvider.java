package com.inox.x45.ocr;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 'azure' profile: calls Azure AI Document Intelligence's prebuilt-invoice
 * model over its REST API (avoiding an extra SDK dependency this offline
 * sandbox can't resolve anyway - see Section 15's note on this environment).
 *
 * BEST-EFFORT / UNVERIFIED: the exact request/response JSON shape (field
 * names under analyzeResult.documents[].fields, the polling contract) is
 * written from documented conventions but has not been run against a live
 * endpoint or compiled in this sandbox. Verify against current Azure AI
 * Document Intelligence docs before relying on this in production - field
 * extraction below fails soft (falls back to nulls) rather than throwing, so
 * a schema mismatch degrades the pre-fill rather than breaking the upload.
 *
 * Also note: this blocks the request thread for up to pollTimeoutSeconds
 * while polling. Fine for a scaffold; a production system should run this
 * as an async job instead of holding an HTTP request open.
 */
@Service
@Profile("azure")
public class AzureDocumentIntelligenceOcrProvider implements OcrProvider {

    private static final String COGNITIVE_SERVICES_SCOPE = "https://cognitiveservices.azure.com/.default";

    private final DocumentIntelligenceProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.azure.identity.DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

    public AzureDocumentIntelligenceOcrProvider(DocumentIntelligenceProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    @Override
    public ExtractedInvoiceData extractInvoiceData(InputStream content, String contentType) {
        try {
            byte[] bytes = content.readAllBytes();
            String bearerToken = fetchAccessToken();

            String analyzeUrl = properties.getEndpoint()
                + "/documentintelligence/documentModels/prebuilt-invoice:analyze?api-version=" + properties.getApiVersion();

            ResponseEntity<Void> submitResponse = restClient.post()
                .uri(analyzeUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes)
                .retrieve()
                .toBodilessEntity();

            String operationLocation = submitResponse.getHeaders().getFirst("Operation-Location");
            if (operationLocation == null) {
                throw new IllegalStateException("Document Intelligence did not return an Operation-Location header");
            }

            JsonNode result = pollUntilDone(operationLocation, bearerToken);
            return toExtractedInvoiceData(result);
        } catch (Exception e) {
            throw new OcrExtractionException("Document Intelligence extraction failed", e);
        }
    }

    private String fetchAccessToken() {
        AccessToken token = credential.getTokenSync(new TokenRequestContext().addScopes(COGNITIVE_SERVICES_SCOPE));
        return token.getToken();
    }

    private JsonNode pollUntilDone(String operationLocation, String bearerToken) throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(properties.getPollTimeoutSeconds());
        while (Instant.now().isBefore(deadline)) {
            String body = restClient.get()
                .uri(operationLocation)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .body(String.class);

            JsonNode json = parseJson(body);
            String status = json.path("status").asText("");
            if ("succeeded".equalsIgnoreCase(status)) {
                return json.path("analyzeResult");
            }
            if ("failed".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Document Intelligence analysis failed: " + body);
            }
            Thread.sleep(1500);
        }
        throw new IllegalStateException("Timed out waiting for Document Intelligence result");
    }

    private JsonNode parseJson(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Document Intelligence response", e);
        }
    }

    private ExtractedInvoiceData toExtractedInvoiceData(JsonNode analyzeResult) {
        JsonNode fields = analyzeResult.path("documents").path(0).path("fields");

        String invoiceNumber = fieldText(fields, "InvoiceId");
        LocalDate invoiceDate = fieldDate(fields, "InvoiceDate");
        String customerName = fieldText(fields, "CustomerName");
        BigDecimal amount = fieldCurrencyAmount(fields, "InvoiceTotal");
        String currency = fieldCurrencyCode(fields, "InvoiceTotal");

        List<ExtractedLineItem> lineItems = new ArrayList<>();
        JsonNode items = fields.path("Items").path("valueArray");
        if (items.isArray()) {
            for (JsonNode item : items) {
                JsonNode itemFields = item.path("valueObject");
                lineItems.add(new ExtractedLineItem(
                    fieldText(itemFields, "Description"),
                    fieldNumber(itemFields, "Quantity"),
                    fieldCurrencyAmount(itemFields, "UnitPrice"),
                    fieldCurrencyAmount(itemFields, "Amount"),
                    null // Document Intelligence's prebuilt-invoice model has no wattage field -
                         // wattage isn't OCR-extractable this way; left for manual entry on review.
                ));
            }
        }

        return new ExtractedInvoiceData(invoiceNumber, invoiceDate, customerName, amount,
            currency == null ? "USD" : currency, lineItems, null);
    }

    private String fieldText(JsonNode fields, String name) {
        JsonNode field = fields.path(name);
        if (field.has("valueString")) {
            return field.path("valueString").asText(null);
        }
        return field.path("content").asText(null);
    }

    private LocalDate fieldDate(JsonNode fields, String name) {
        String value = fields.path(name).path("valueDate").asText(null);
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal fieldCurrencyAmount(JsonNode fields, String name) {
        JsonNode currency = fields.path(name).path("valueCurrency");
        if (currency.has("amount")) {
            return new BigDecimal(currency.path("amount").asText());
        }
        return null;
    }

    private String fieldCurrencyCode(JsonNode fields, String name) {
        JsonNode currency = fields.path(name).path("valueCurrency");
        return currency.has("currencyCode") ? currency.path("currencyCode").asText(null) : null;
    }

    private BigDecimal fieldNumber(JsonNode fields, String name) {
        JsonNode field = fields.path(name);
        if (field.has("valueNumber")) {
            return new BigDecimal(field.path("valueNumber").asText());
        }
        return null;
    }
}
