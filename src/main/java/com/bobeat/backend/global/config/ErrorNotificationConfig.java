package com.bobeat.backend.global.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 에러 알림 설정을 관리하는 Configuration
 */
@Configuration
@ConfigurationProperties(prefix = "error.notification")
@Getter
@Setter
public class ErrorNotificationConfig {

    private boolean enabled = true;
    private boolean includeClientErrors = true;
    private List<String> excludedPaths;
    private int maxStackTraceLines = 10;

    /**
     * 특정 경로가 알림에서 제외되는지 확인합니다.
     */
    public boolean isPathExcluded(String path) {
        if (excludedPaths == null) {
            return false;
        }
        return excludedPaths.stream().anyMatch(path::startsWith);
    }

    /**
     * 디스코드 알림 활성화 여부를 확인합니다.
     */
    public boolean isDiscordNotificationEnabled() {
        return enabled;
    }
} 
