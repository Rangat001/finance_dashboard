package com.example.finance.finance.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//                          Swagger/OpenAPI configuration for API documentation and JWT security scheme
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Finanace Dashboard API",
                version = "1.0",
                description = "Finance Managment",
                contact = @Contact(name = "API Support", email = "rangatprajapati@gmail.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication token"
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Finance Dashboard API")
                        .version("1.0")
                        .description("API documentation")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("API Support")
                                .email("rangatprajapati@gmail.com")));
    }
}
