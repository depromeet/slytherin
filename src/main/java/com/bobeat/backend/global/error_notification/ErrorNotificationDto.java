package com.bobeat.backend.global.error_notification;

import com.bobeat.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 에러 알림에 필요한 데이터를 담는 DTO
 */
@Data
@Builder
public class ErrorNotificationDto {

    private String timestamp;
    private String errorCode;
    private String errorMessage;
    private String exceptionType;
    private String exceptionMessage;
    private String requestUri;
    private String requestMethod;
    private String userAgent;
    private String clientIp;
    private String requestParams;
    private String requestBody;
    private String requestHeaders;
    private String stackTrace;
    private List<ExceptionChainDto> exceptionChain; // 예외 체인 정보 추가

    @Data
    @Builder
    public static class ExceptionChainDto {
        private String exceptionType;
        private String message;
        private String stackTrace;
        private int order; // 예외 발생 순서 (0: 최상위, 1: 첫 번째 원인, ...)
    }

    public static ErrorNotificationDto create(ErrorCode errorCode, Exception exception,
                                              HttpServletRequest request) {

        String requestBody = "";
        if (request instanceof ContentCachingRequestWrapper) {
            requestBody = getRequestBody((ContentCachingRequestWrapper) request);
        }

        return ErrorNotificationDto.builder()
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .errorCode(errorCode.getCode())
                .errorMessage(errorCode.getMessage())
                .exceptionType(exception.getClass().getSimpleName())
                .exceptionMessage(exception.getMessage())
                .requestUri(request.getRequestURI())
                .requestMethod(request.getMethod())
                .userAgent(request.getHeader("User-Agent"))
                .clientIp(getClientIp(request))
                .requestParams(getRequestParams(request))
                .requestBody(requestBody)
                .requestHeaders(getRequestHeaders(request))
                .stackTrace(getStackTraceAsString(exception))
                .exceptionChain(buildExceptionChain(exception)) // 예외 체인 구축
                .build();
    }

    /**
     * 예외 체인을 분석하여 상위 예외부터 하위 원인 예외까지 순서대로 구성
     */
    private static List<ExceptionChainDto> buildExceptionChain(Exception exception) {
        List<ExceptionChainDto> chain = new ArrayList<>();
        Throwable current = exception;
        int order = 0;

        while (current != null) {
            chain.add(ExceptionChainDto.builder()
                    .exceptionType(current.getClass().getSimpleName())
                    .message(current.getMessage())
                    .stackTrace(getStackTraceForException(current, 5)) // 각 예외당 5줄로 제한
                    .order(order++)
                    .build());

            current = current.getCause();
        }

        return chain;
    }

    /**
     * 특정 예외의 스택 트레이스를 제한된 줄 수와 문자 수로 반환
     */
    private static String getStackTraceForException(Throwable exception, int maxLines) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = exception.getStackTrace();
        
        int maxChars = 800; // 메시지와 합쳐도 1024자를 넘지 않도록 여유있게 설정
        int linesToInclude = Math.min(stackTrace.length, maxLines);
        
        for (int i = 0; i < linesToInclude; i++) {
            String line = stackTrace[i].toString() + "\n";
            
            // 추가했을 때 최대 문자 수를 넘으면 중단
            if (sb.length() + line.length() > maxChars) {
                sb.append("... (길이 제한으로 생략)");
                break;
            }
            
            sb.append(line);
        }

        // 라인 수 제한으로 생략된 경우에만 표시 (문자 수 제한으로 생략된 경우 위에서 이미 표시)
        if (stackTrace.length > maxLines && sb.length() <= maxChars) {
            sb.append("... (총 ").append(stackTrace.length).append("줄)");
        }

        return sb.toString();
    }

    private static String getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }

        if (headers.isEmpty()) {
            return "No Headers";
        }
        return headers.toString();
    }

    private static String getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }

        if (params.isEmpty()) {
            return "No Parameters";
        }
        return params.toString();
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "No Body";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * 스택 트레이스를 문자열로 변환합니다.
     */
    private static String getStackTraceAsString(Exception exception) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // 설정된 최대 라인 수까지만 포함
        int linesToInclude = Math.min(stackTrace.length, 10);
        for (int i = 0; i < linesToInclude; i++) {
            sb.append(stackTrace[i].toString()).append("\n");
        }

        if (stackTrace.length > 10) {
            sb.append("... (총 ").append(stackTrace.length).append("줄)");
        }

        return sb.toString();
    }

    /**
     * 클라이언트 IP 주소를 가져옵니다.
     */
    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
} 
