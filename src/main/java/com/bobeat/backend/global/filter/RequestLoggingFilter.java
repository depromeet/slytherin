package com.bobeat.backend.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 요청 ID 생성
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        // 요청/응답 래핑 (body를 여러 번 읽기 위해)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // 요청 기본 정보 로깅
            logRequestBasicInfo(wrappedRequest, requestId);

            // 다음 필터 체인 실행
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // 필터 체인 실행 후 body 로깅 (이때 body가 캐시됨)
            logRequestBody(wrappedRequest, requestId);

            // 응답 body를 실제 response에 복사 (중요!)
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequestBasicInfo(ContentCachingRequestWrapper request, String requestId) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("🚀 [{}] [{}] {} {} {} - IP: {} - User-Agent: {}",
                requestId, timestamp, method, uri,
                queryString != null ? "?" + queryString : "",
                clientIp, userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 100)) : "Unknown");

        // 헤더 로깅 (민감한 정보 제외)
        logHeaders(request, requestId);

    }

    private void logHeaders(HttpServletRequest request, String requestId) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append(", ");
        }

        if (!headers.isEmpty()) {
            headers.setLength(headers.length() - 2); // 마지막 ", " 제거
            log.debug("📋 [{}] Headers: {{{}}}", requestId, headers);
        }
    }

    private void logRequestBody(ContentCachingRequestWrapper request, String requestId) {
        // Body 로깅 조건 체크
        if (!shouldLogRequestBody(request)) {
            return;
        }

        byte[] content = request.getContentAsByteArray();
        log.debug("📝 [{}] Body 길이: {}", requestId, content.length);

        if (content.length > 0) {
            try {
                String encoding = request.getCharacterEncoding();
                String body = new String(content, encoding != null ? encoding : "UTF-8");

                // body가 너무 길면 자르기
                if (body.length() > 1000) {
                    body = body.substring(0, 1000) + "... (truncated)";
                }

                log.info("📝 [{}] Request Body: {}", requestId, body);
            } catch (UnsupportedEncodingException e) {
                log.warn("📝 [{}] Request Body: Cannot decode body - {}", requestId, e.getMessage());
            }
        } else {
            log.debug("📝 [{}] Request Body가 비어있음", requestId);
        }
    }

    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();
        String uri = request.getRequestURI();

        log.debug("Body 로깅 조건 체크 - URI: {}, Method: {}, ContentType: {}", uri, method, contentType);

        // POST, PUT, PATCH만 body 로깅
        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method)) {
            log.debug("Body 로깅 안함 - 메서드가 POST/PUT/PATCH가 아님: {}", method);
            return false;
        }

        // JSON, form-data만 로깅
        boolean shouldLog = contentType != null &&
                (contentType.contains("application/json") ||
                        contentType.contains("application/x-www-form-urlencoded"));

        if (!shouldLog) {
            log.debug("Body 로깅 안함 - ContentType이 JSON/form-data가 아님: {}", contentType);
        }

        return shouldLog;
    }

    private String getClientIpAddress(HttpServletRequest request) {
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


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 정적 리소스, 헬스체크, 스웨거는 로깅 제외
        return uri.startsWith("/static/") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/images/") ||
                uri.startsWith("/swagger-ui/") ||
                uri.startsWith("/v3/api-docs") ||
                uri.equals("/health") ||
                uri.equals("/actuator/health");
    }
}
