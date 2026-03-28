package com.straykun.svd.svdsys.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置类。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 构建项目的 OpenAPI 文档基础信息，并声明 JWT Bearer 鉴权方案。
     *
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI svdOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("互感器检定系统 API")
                        .description("后端接口文档（Swagger UI）")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
