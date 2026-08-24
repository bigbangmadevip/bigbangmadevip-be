package com.thevip.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 요청 처리 중 찍히는 모든 로그(Hibernate SQL 포함)에 "어떤 API 요청을, 누가 호출했는지"를 붙이기 위한 필터.
 * MDC에 넣어두면 logging.pattern.console의 %X{requestUri}, %X{user}로 모든 로그 줄에 자동 반영된다.
 * 추가로 요청이 끝나면 호출자/method/URI/상태코드/처리시간을 한 줄 요약으로 남긴다. 헬스체크(/health)는
 * 로드밸런서가 주기적으로 찔러서 로그 노이즈만 되므로 요약 로그에서는 제외한다.
 *
 * JSON 요청이 4xx/5xx로 끝나면 요청 바디도 같이 남긴다 — 검증 실패/역직렬화 실패는
 * GlobalExceptionHandler가 상세 로그를 안 남기는 경우가 많아서, 그때 "정확히 뭘 보냈는지"를
 * 알아야 원인을 알 수 있다. multipart(파일 업로드)는 바디가 커서 캐싱 대상에서 제외한다.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String USER_MDC_KEY = "user";
    private static final int MAX_LOGGED_BODY_LENGTH = 2000;
    // ContentCachingRequestWrapper가 이 바이트 수까지만 버퍼링한다. 로그에 실제로 찍는 길이(위
    // MAX_LOGGED_BODY_LENGTH)보다 넉넉히 잡아둬야 "...(truncated)" 여부를 정확히 판단할 수 있다.
    private static final int MAX_CACHED_BODY_BYTES = 8192;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MDC.put(REQUEST_URI_MDC_KEY, request.getMethod() + " " + request.getRequestURI());
        String user = resolveUser();
        MDC.put(USER_MDC_KEY, user);
        long startedAt = System.currentTimeMillis();

        boolean cacheBody = isJsonRequest(request);
        HttpServletRequest requestToUse = cacheBody
                ? new ContentCachingRequestWrapper(request, MAX_CACHED_BODY_BYTES) : request;
        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            if (!"/health".equals(request.getRequestURI())) {
                long elapsedMs = System.currentTimeMillis() - startedAt;
                log.info("[{}] {} {} -> {} ({}ms)", user, request.getMethod(), request.getRequestURI(),
                        response.getStatus(), elapsedMs);
                if (cacheBody && response.getStatus() >= 400) {
                    logRequestBody((ContentCachingRequestWrapper) requestToUse);
                }
            }
            MDC.remove(REQUEST_URI_MDC_KEY);
            MDC.remove(USER_MDC_KEY);
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith("application/json");
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] body = request.getContentAsByteArray();
        if (body.length == 0) {
            return;
        }
        String content = new String(body, StandardCharsets.UTF_8);
        if (content.length() > MAX_LOGGED_BODY_LENGTH) {
            content = content.substring(0, MAX_LOGGED_BODY_LENGTH) + "...(truncated)";
        }
        log.warn("요청 바디: {}", content);
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            return oauth.getAuthorizedClientRegistrationId() + ":" + oauth.getName();
        }
        return "anonymous";
    }
}
