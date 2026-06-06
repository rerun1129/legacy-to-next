package com.freightos.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PMS API")
                        .description("Performance Management System — Java Backend")
                        .version("v1.0")
                        .contact(new Contact().name("FreightOS").email("dev@freightos.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Local"),
                        new Server().url("https://api.freightos.com").description("Production")));
    }
}
