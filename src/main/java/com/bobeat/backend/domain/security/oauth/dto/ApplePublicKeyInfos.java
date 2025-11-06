package com.bobeat.backend.domain.security.oauth.dto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApplePublicKeyInfos {

    private List<ApplePublicKeyInfo> keys;

}
