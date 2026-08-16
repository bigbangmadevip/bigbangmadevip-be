package com.thevip.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 탈취된 세션이 활동만으로 무기한 살아있는 것을 막기 위해, 최초 로그인 시점부터 90일이 지나면
 * 활동 여부와 무관하게 세션을 무효화한다 (절대 상한). 60일 슬라이딩 갱신은 Spring Session이
 * 매 요청마다 Redis TTL을 갱신해주는 방식으로 대신한다 (spring.session.timeout).
 */
@Component
public class SessionRenewalFilter extends OncePerRequestFilter {

    private static final String CREATED_AT_KEY = "SESSION_CREATED_AT";
    private static final Duration ABSOLUTE_MAX_AGE = Duration.ofDays(90);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Instant createdAt = (Instant) session.getAttribute(CREATED_AT_KEY);
            if (createdAt == null) {
                session.setAttribute(CREATED_AT_KEY, Instant.now());
            } else if (Instant.now().isAfter(createdAt.plus(ABSOLUTE_MAX_AGE))) {
                session.invalidate();
            }
        }
        filterChain.doFilter(request, response);
    }
}
