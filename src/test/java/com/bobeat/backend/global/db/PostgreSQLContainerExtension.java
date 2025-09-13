package com.bobeat.backend.global.db;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQLContainerExtension {

    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer() {
        DockerImageName postgisImage = DockerImageName.parse("postgis/postgis:15-3.3")
                .asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer<>(postgisImage);
    }
}
