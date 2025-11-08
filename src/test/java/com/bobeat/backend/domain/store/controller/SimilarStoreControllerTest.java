package com.bobeat.backend.domain.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobeat.backend.domain.store.dto.request.EditProposalRequest;
import com.bobeat.backend.domain.store.dto.response.EditProposalResponse;
import com.bobeat.backend.domain.store.entity.ProposalType;
import com.bobeat.backend.domain.store.service.SimilarStoreService;
import com.bobeat.backend.domain.store.service.StoreProposalService;
import com.bobeat.backend.domain.store.service.StoreService;
import com.bobeat.backend.global.error_notification.ErrorNotificationService;
import com.bobeat.backend.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@Import(GlobalExceptionHandler.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private SimilarStoreService similarStoreService;

    @MockBean
    private StoreProposalService storeProposalService;

    @MockBean
    private ErrorNotificationService errorNotificationService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유사 가게 추천 API 정상 호출 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testProposeEdit() throws Exception {
        // given
        Long storeId = 1L;
        ProposalType proposalType = ProposalType.STORE_CLOSED;
        String content = "내용";

        EditProposalResponse response = EditProposalResponse.builder()
                .id(1L)
                .proposalType(proposalType)
                .content("내용")
                .build();
        EditProposalRequest request = EditProposalRequest.builder()
                .proposalType(proposalType)
                .content(content)
                .build();

        given(storeProposalService.proposeEditWithoutMember(eq(storeId), any(EditProposalRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/stores/{storeId}/proposals", storeId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.id").value(1L))
                .andExpect(jsonPath("$.response.proposalType").value("STORE_CLOSED"))
                .andExpect(jsonPath("$.response.content").value("내용"));
    }
}
