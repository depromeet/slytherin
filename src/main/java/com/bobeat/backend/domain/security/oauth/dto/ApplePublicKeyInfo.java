package com.bobeat.backend.domain.security.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ApplePublicKeyInfo {

    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;
}
