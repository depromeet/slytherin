package com.bobeat.backend.global.db;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLContainerExtension implements BeforeAllCallback {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        POSTGRES_CONTAINER.start();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRES_CONTAINER.getPassword(),
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
            ).applyTo(context.getEnvironment());
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        // 아무 것도 안 해도 됨 - 컨테이너는 static 블럭에서 이미 시작됨
    }
}