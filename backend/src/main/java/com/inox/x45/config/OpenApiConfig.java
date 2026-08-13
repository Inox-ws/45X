package com.inox.x45.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI x45OpenApi() {
        final String bearerScheme = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("45X Portal API")
                .description("Section 45X advanced manufacturing production credit - traceability, "
                    + "FEOC/PFE compliance, credit calculation, and reporting API")
                .version("v1")
                .contact(new Contact().name("45X Portal Engineering")))
            .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
            .components(new Components().addSecuritySchemes(bearerScheme,
                new SecurityScheme()
                    .name(bearerScheme)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
