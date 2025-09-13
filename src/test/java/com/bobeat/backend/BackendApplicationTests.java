package com.bobeat.backend;

import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@PostgreSQLTestContainer
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
