package com.bobeat.backend.domain.security.auth.dto;


import com.bobeat.backend.domain.member.entity.MemberRole;
import com.bobeat.backend.domain.member.entity.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long memberId;
    private String nickName;
    @Schema(description = "혼밥 레벨")
    int level;
    private String profileImage;
    private SocialProvider providerType;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
    private MemberRole memberRole;
}
