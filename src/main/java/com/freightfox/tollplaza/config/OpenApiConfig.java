package com.freightfox.tollplaza.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Toll Plaza Route Finder API")
                        .version("1.0.0")
                        .description("REST API to determine toll plazas located along the route between two Indian pincodes.")
                        .contact(new Contact()
                                .name("Freightfox Backend Team")
                                .email("engineering@freightfox.ai")));
    }
}
