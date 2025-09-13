package com.bobeat.backend.domain.security.auth.domain;

import com.bobeat.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class RefreshToken extends BaseTimeEntity {
    @Id
    private Long memberId;

    @Column(name = "refresh_token", length = 4096)
    private String refreshToken;

    @Builder
    public RefreshToken(Long memberId, String refreshToken) {
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        //this.refreshExpiration = refreshExpiration;
    }

    public RefreshToken updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
        return this;
    }
}
