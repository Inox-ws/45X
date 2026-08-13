package com.inox.x45.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 'azure' profile only: identifies which Entra ID tenant and API app
 * registration issue/receive tokens for this backend (Section 3, Section 12).
 * None of these are secrets - the resource server only ever validates
 * incoming tokens, it never calls out to Entra ID itself.
 */
@Component
@ConfigurationProperties(prefix = "x45.entra")
public class EntraProperties {

    /** Entra ID tenant ID (GUID) or primary domain, e.g. contoso.onmicrosoft.com. PLACEHOLDER - set per environment. */
    private String tenantId;

    /** The backend API app registration's Application ID URI or client ID - expected JWT audience. PLACEHOLDER - set per environment. */
    private String apiAudience;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApiAudience() {
        return apiAudience;
    }

    public void setApiAudience(String apiAudience) {
        this.apiAudience = apiAudience;
    }
}
