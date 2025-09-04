package com.bobeat.backend.global.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("밥잇 API 명세서")
                        .description("작성중입니다")
                        .version("1.0.0")
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth")) // SecurityRequirement 추가
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))); // JWT 인증 적용
    }

    // Swagger UI의 동작을 제어
    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters(SwaggerUiConfigProperties swaggerUiConfigProperties) {
        SwaggerUiConfigParameters parameters = new SwaggerUiConfigParameters(swaggerUiConfigProperties);

        // 같은 api 접두사 내에 정렬
        parameters.setOperationsSorter("method");

        // api 별로 정렬
        parameters.setTagsSorter("alpha");

        // 검색창 활성화
        parameters.setFilter("true");

        // 브라우저 닫더라도 토큰 유효하게
        parameters.setPersistAuthorization(true);
        parameters.setDocExpansion("none");

        return parameters;
    }
}
