package com.assignment.shop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop E-Commerce API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Danish Ahmed")
                                .url("https://github.com/danishahm89/Shop-Assignment")
                                .email("danishahm89@gmail.com"))
                        .license(new License().name("Apache 2.0")
                                .url("http://springdoc.org")));

    }
}

