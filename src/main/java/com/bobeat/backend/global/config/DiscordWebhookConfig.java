package com.bobeat.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Discord 웹훅 관련 설정을 관리하는 Configuration
 */
@Configuration
@Getter
public class DiscordWebhookConfig {

    @Value("${discord.webhook.url:}")
    private String webhookUrl;


    public boolean isWebhookConfigured() {
        return webhookUrl != null && !webhookUrl.trim().isEmpty();
    }
} 
