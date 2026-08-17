package com.thevip.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청 처리 중 찍히는 모든 로그(Hibernate SQL 포함)에 "어떤 API 요청이 트리거했는지"를 붙이기 위한 필터.
 * MDC에 넣어두면 logging.pattern.console의 %X{requestUri}로 모든 로그 줄에 자동 반영된다.
 * 추가로 요청이 끝나면 method/URI/상태코드/처리시간을 한 줄 요약으로 남긴다. 헬스체크(/health)는
 * 로드밸런서가 주기적으로 찔러서 로그 노이즈만 되므로 요약 로그에서는 제외한다.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "requestUri";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MDC.put(MDC_KEY, request.getMethod() + " " + request.getRequestURI());
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!"/health".equals(request.getRequestURI())) {
                long elapsedMs = System.currentTimeMillis() - startedAt;
                log.info("{} {} -> {} ({}ms)", request.getMethod(), request.getRequestURI(), response.getStatus(),
                        elapsedMs);
            }
            MDC.remove(MDC_KEY);
        }
    }
}
