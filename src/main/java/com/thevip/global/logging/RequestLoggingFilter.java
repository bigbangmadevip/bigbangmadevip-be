package com.thevip.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청 처리 중 찍히는 모든 로그(Hibernate SQL 포함)에 "어떤 API 요청이 트리거했는지"를 붙이기 위한 필터.
 * MDC에 넣어두면 logging.pattern.console의 %X{requestUri}로 모든 로그 줄에 자동 반영된다.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "requestUri";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        MDC.put(MDC_KEY, request.getMethod() + " " + request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
