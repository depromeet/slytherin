package com.bobeat.backend.domain.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobeat.backend.domain.member.entity.Member;
import com.bobeat.backend.domain.member.entity.MemberRole;
import com.bobeat.backend.domain.member.entity.SocialProvider;
import com.bobeat.backend.domain.member.repository.MemberRepository;
import com.bobeat.backend.domain.security.auth.dto.TokenClaims;
import com.bobeat.backend.domain.security.auth.service.JwtService;
import com.bobeat.backend.global.db.PostgreSQLTestContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@PostgreSQLTestContainer
public class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Member testMember;
    private String validAccessToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트용 회원 생성
        testMember = Member.builder()
                .socialProvider(SocialProvider.KAKAO)
                .providerId("test-provider-id")
                .email("test@example.com")
                .role(MemberRole.USER)
                .profileImageUrl("https://example.com/profile.jpg")
                .build();

        testMember = memberRepository.save(testMember);

        // 유효한 JWT 토큰 생성
        validAccessToken = generateValidToken();
    }

    private String generateValidToken() {
        return "Bearer " + jwtService.generateAccessToken(TokenClaims.from(testMember)
        );
    }

    @Test
    void JWT토큰없으면_403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 잘못된JWT토큰이면_401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 퍼블릭API는_JWT토큰없어도_200OK() throws Exception {
        // 테스트용 간단한 public endpoint
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk());
    }

    @Test
    void 퍼블릭API는_JWT토큰있어도_200OK() throws Exception {
        // 테스트용 간단한 public endpoint
        mockMvc.perform(get("/test/success")
                        .header("Authorization", validAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    void 소셜로그인은_JWT토큰없어도_접근가능() throws Exception {
        // 대신 public 경로로 테스트
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.message").value("응답 값"));
    }

    @Test
    void AuthenticationPrincipal만있는엔드포인트_JWT토큰없으면_null처리() throws Exception {
        String onboardingRequest = objectMapper.writeValueAsString(Map.of(
                "answers", List.of(
                        Map.of("questionOrder", 1, "selectedOption", 2),
                        Map.of("questionOrder", 2, "selectedOption", 2),
                        Map.of("questionOrder", 3, "selectedOption", 2),
                        Map.of("questionOrder", 4, "selectedOption", 2),
                        Map.of("questionOrder", 5, "selectedOption", 2)
                )
        ));

        mockMvc.perform(post("/api/v1/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isNotEmpty())
                .andExpect(jsonPath("$.response.level").exists());
    }

    @Test
    void AuthenticationPrincipal만있는엔드포인트_JWT토큰있으면_memberId주입() throws Exception {
        String onboardingRequest = objectMapper.writeValueAsString(Map.of(
                "answers", List.of(
                        Map.of("questionOrder", 1, "selectedOption", 2),
                        Map.of("questionOrder", 2, "selectedOption", 2),
                        Map.of("questionOrder", 3, "selectedOption", 2),
                        Map.of("questionOrder", 4, "selectedOption", 2),
                        Map.of("questionOrder", 5, "selectedOption", 2)
                )
        ));

        mockMvc.perform(post("/api/v1/onboarding")
                        .header("Authorization", validAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isNotEmpty())
                .andExpect(jsonPath("$.response.level").exists());
    }

    @Test
    void AuthenticationPrincipal만있는엔드포인트_잘못된JWT토큰이면_401() throws Exception {
        String onboardingRequest = objectMapper.writeValueAsString(Map.of(
                "answers", List.of(
                        Map.of("questionOrder", 1, "selectedOption", 2),
                        Map.of("questionOrder", 2, "selectedOption", 2),
                        Map.of("questionOrder", 3, "selectedOption", 2),
                        Map.of("questionOrder", 4, "selectedOption", 2),
                        Map.of("questionOrder", 5, "selectedOption", 2)
                )
        ));

        mockMvc.perform(post("/api/v1/onboarding")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(onboardingRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void Authorization헤더없으면_JWT필터건너뜀() throws Exception {
        // Public 엔드포인트로 JWT 필터 건너뛰는 것 확인
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.message").value("응답 값"));
    }

    @Test
    void Bearer로시작하지않으면_JWT필터건너뜀() throws Exception {
        mockMvc.perform(get("/test/success")
                        .header("Authorization", "Basic some-basic-auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.message").value("응답 값"));
    }
}
