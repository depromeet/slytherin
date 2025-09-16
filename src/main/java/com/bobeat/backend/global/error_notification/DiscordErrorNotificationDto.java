package com.bobeat.backend.global.error_notification;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordErrorNotificationDto {
    private final String title;
    private final String description;
    private final int color;
    private final String timestamp;
    private final List<Map<String, Object>> fields;

    public DiscordErrorNotificationDto(ErrorNotificationDto dto) {
        this.title = determineTitle(dto);
        this.color = determineColor(dto);
        this.description = buildDescription(dto);
        this.timestamp = getIsoTimestamp();

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.addAll(splitIntoFields("Request Headers", dto.getRequestHeaders()));
        fields.addAll(splitIntoFields("Request Parameters", dto.getRequestParams()));
        fields.addAll(splitIntoFields("Request Body", dto.getRequestBody()));
        
        // 예외 체인을 구분해서 표시
        addExceptionChainFields(fields, dto);
        
        this.fields = fields;
    }

    /**
     * 에러 타입에 따른 제목 결정
     */
    private String determineTitle(ErrorNotificationDto dto) {
        if (dto.getErrorCode().startsWith("4")) {
            return "⚠️ 클라이언트 에러 발생";
        } else if (dto.getErrorCode().startsWith("5")) {
            return "🚨 서버 에러 발생";
        } else {
            return "❗ 에러 발생";
        }
    }

    /**
     * 에러 타입에 따른 색상 결정
     */
    private int determineColor(ErrorNotificationDto dto) {
        if (dto.getErrorCode().startsWith("4")) {
            return 16776960; // 노란색 (클라이언트 에러)
        } else if (dto.getErrorCode().startsWith("5")) {
            return 16711680; // 빨간색 (서버 에러)
        } else {
            return 8421504; // 회색 (기타)
        }
    }

    /**
     * 예외 체인을 구분해서 필드에 추가
     */
    private void addExceptionChainFields(List<Map<String, Object>> fields, ErrorNotificationDto dto) {
        if (dto.getExceptionChain() == null || dto.getExceptionChain().isEmpty()) {
            // 기존 방식으로 폴백
            fields.add(Map.of(
                    "name", "스택 트레이스",
                    "value", "```" + truncate(dto.getStackTrace(), 1000) + "```",
                    "inline", false
            ));
            return;
        }

        for (ErrorNotificationDto.ExceptionChainDto exception : dto.getExceptionChain()) {
            String fieldName = getExceptionFieldName(exception);
            String exceptionInfo = buildExceptionInfo(exception);
            
            fields.add(Map.of(
                    "name", fieldName,
                    "value", "```" + exceptionInfo + "```",
                    "inline", false
            ));
        }
    }

    /**
     * 예외 순서에 따른 필드 이름 생성
     */
    private String getExceptionFieldName(ErrorNotificationDto.ExceptionChainDto exception) {
        if (exception.getOrder() == 0) {
            return "- 예외: " + exception.getExceptionType();
        } else {
            return "- 상위 원인: " + exception.getExceptionType();
        }
    }

    /**
     * 예외 정보를 문자열로 구성 (메시지 + 스택 트레이스)
     */
    private String buildExceptionInfo(ErrorNotificationDto.ExceptionChainDto exception) {
        StringBuilder sb = new StringBuilder();

        // 예외 메시지 추가
        if (exception.getMessage() != null && !exception.getMessage().isEmpty()) {
            sb.append("메시지: ").append(exception.getMessage()).append("\n\n");
        }

        // 스택 트레이스 추가
        if (exception.getStackTrace() != null && !exception.getStackTrace().isEmpty()) {
            sb.append("스택 트레이스:\n").append(exception.getStackTrace());
        }

        // 아무것도 없으면 예외 타입만 반환
        if (sb.length() == 0) {
            return exception.getExceptionType();
        }

        return truncate(sb.toString(), 900);
    }

    public Map<String, Object> toEmbed() {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", title);
        embed.put("description", description);
        embed.put("color", color);
        embed.put("timestamp", timestamp);
        embed.put("fields", fields);
        return embed;
    }

    private static String buildDescription(ErrorNotificationDto dto) {
        return String.format(
                "**에러 코드**: %s\n" +
                        "**에러 메시지**: %s\n" +
                        "**예외 타입**: %s\n" +
                        "**예외 메시지**: %s\n" +
                        "**요청 URI**: %s\n" +
                        "**요청 메소드**: %s\n" +
                        "**클라이언트 IP**: %s\n" +
                        "**User-Agent**: %s",
                safe(dto.getErrorCode()),
                safe(dto.getErrorMessage()),
                safe(dto.getExceptionType()),
                truncate(dto.getExceptionMessage(), 100),
                safe(dto.getRequestUri()),
                safe(dto.getRequestMethod()),
                safe(dto.getClientIp()),
                truncate(dto.getUserAgent(), 100)
        );
    }

    private static String getIsoTimestamp() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String truncate(String str, int max) {
        if (str == null) {
            return "";
        }
        if (str.length() <= max) {
            return str;
        }
        return str.substring(0, max) + "...";
    }

    private static List<Map<String, Object>> splitIntoFields(String name, String value) {
        List<Map<String, Object>> fields = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            fields.add(Map.of(
                    "name", name,
                    "value", "``` ```",
                    "inline", false
            ));
            return fields;
        }

        int maxChunkSize = 900; // 디스코드 1024자 제한에 여유 두기
        int valueLength = value.length();
        int partNumber = 1;
        for (int i = 0; i < valueLength; i += maxChunkSize) {
            String chunk = value.substring(i, Math.min(valueLength, i + maxChunkSize));
            String fieldName = valueLength > maxChunkSize ? String.format("%s (%d)", name, partNumber++) : name;
            fields.add(Map.of(
                    "name", fieldName,
                    "value", "```" + chunk + "```",
                    "inline", false
            ));
        }
        return fields;
    }

} 
