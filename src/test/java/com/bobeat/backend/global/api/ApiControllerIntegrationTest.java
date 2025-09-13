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
    void 전역_응답_값을_사용한다() throws Exception {
        mockMvc.perform(get("/test/success"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response.message").value("응답 값"))
            .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void 전역_예외_값을_확인한다() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(jsonPath("$.response").doesNotExist())
            .andExpect(jsonPath("$.errorResponse.code").value("G500"))
            .andExpect(jsonPath("$.errorResponse.message").value("서버 내부에서 에러가 발생하였습니다"));
    }

    @Test
    void 전역_예외만_생성_할_수_있다() throws Exception {
        mockMvc.perform(get("/test/success-only"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void 커스텀_예외를_발생시킨다() throws Exception {
        mockMvc.perform(get("/test/custom-error"))
                .andExpect(jsonPath("$.response").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.code").value("G500"))
                .andExpect(jsonPath("$.errorResponse.message").value("서버 내부에서 에러가 발생하였습니다"));
    }

    @Test
    void 커스텀_예외를_메세지와_함께_발생시킨다() throws Exception {
        mockMvc.perform(get("/test/custom-error/message"))
                .andExpect(jsonPath("$.response").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.code").value("G500"))
                .andExpect(jsonPath("$.errorResponse.message").value("허용되지 않은 API입니다"));
    }
}
