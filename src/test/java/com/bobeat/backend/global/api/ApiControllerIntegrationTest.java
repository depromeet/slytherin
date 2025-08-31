package com.bobeat.backend.global.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSuccessResponse() throws Exception {
        mockMvc.perform(get("/test/success"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.response.message").value("응답 값"))
            .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void testErrorResponse() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.response").doesNotExist())
            .andExpect(jsonPath("$.errorResponse.code").value("G500"))
            .andExpect(jsonPath("$.errorResponse.message").value("서버 내부에서 에러가 발생하였습니다"));
    }

    @Test
    void testSuccessOnlyResponse() throws Exception {
        mockMvc.perform(get("/test/success-only"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.error").doesNotExist());
    }
}