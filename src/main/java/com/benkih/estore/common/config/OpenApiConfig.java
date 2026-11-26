package com.benkih.estore.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Central OpenAPI definition for the E-Store REST API. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI estoreOpenApi(
            @Value("${openapi.server-url:http://localhost:8080}") String serverUrl,
            @Value("${openapi.server-description:Local development}") String serverDescription,
            @Value("${info.app.version:1.0.0}") String appVersion) {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("E-Store API")
                                .description(
                                        """
                    Multi-tenant e-commerce REST API.

                    Base path: /api/v1 (override with API_PREFIX).

                    Public: login, registration, email verification, password reset,
                    product browsing, payment callbacks and gateway webhooks.

                    Protected: everything else. Obtain a token from POST /api/v1/auth/login
                    and send it as: Authorization: Bearer <token>.
                    """)
                                .version(appVersion)
                                .contact(new Contact().name("E-Store")))
                .addServersItem(new Server().url(serverUrl).description(serverDescription))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        BEARER_JWT,
                                        new SecurityScheme()
                                                .name(BEARER_JWT)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Access token issued by the auth endpoints.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                .addTagsItem(
                        new Tag()
                                .name("Auth")
                                .description(
                                        "Login, registration, verification and password reset"))
                .addTagsItem(
                        new Tag()
                                .name("Users")
                                .description("User profile and account administration"))
                .addTagsItem(new Tag().name("Addresses").description("User delivery addresses"))
                .addTagsItem(
                        new Tag()
                                .name("Bank Accounts")
                                .description("Seller bank accounts for payouts"))
                .addTagsItem(
                        new Tag()
                                .name("Departments")
                                .description("Business departments and their members"))
                .addTagsItem(
                        new Tag()
                                .name("Permissions")
                                .description("Available permission authorities"))
                .addTagsItem(
                        new Tag()
                                .name("Roles")
                                .description("Roles and their permission assignments"))
                .addTagsItem(new Tag().name("Products").description("Product catalogue and search"))
                .addTagsItem(new Tag().name("Categories").description("Product categories"))
                .addTagsItem(
                        new Tag().name("Images").description("Product image upload and management"))
                .addTagsItem(
                        new Tag()
                                .name("Inventory")
                                .description("Stock levels, additions and damage records"))
                .addTagsItem(new Tag().name("Carts").description("Current user's shopping cart"))
                .addTagsItem(
                        new Tag()
                                .name("Cart Items")
                                .description("Adding, updating and removing cart items"))
                .addTagsItem(
                        new Tag()
                                .name("Orders")
                                .description("Placing and tracking buyer and seller orders"))
                .addTagsItem(
                        new Tag()
                                .name("Payments")
                                .description("Checkout initiation and payment verification"))
                .addTagsItem(new Tag().name("Payouts").description("Seller payout requests"))
                .addTagsItem(
                        new Tag()
                                .name("Refunds")
                                .description("Refund requests, approvals and gateway verification"))
                .addTagsItem(
                        new Tag().name("Webhooks").description("Inbound payment gateway webhooks"));
    }
}
