package com.hiremate.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireMate AI API")
                        .description("AI智能面试系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HireMate")
                                .email("support@hiremate.com")));
    }
}
