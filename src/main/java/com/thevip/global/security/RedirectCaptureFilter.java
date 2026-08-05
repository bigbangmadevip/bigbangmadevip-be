package com.thevip.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * /oauth2/authorization/{provider}?redirect=http://localhost:3000 로 로그인을 시작하면
 * 그 값을 세션에 저장해뒀다가, 로그인 성공 후 DynamicRedirectSuccessHandler가 꺼내 씀.
 * 로컬 프론트에서 배포된 백엔드로 로그인 붙일 때 콜백을 로컬로 돌려보내기 위한 용도.
 */
public class RedirectCaptureFilter extends OncePerRequestFilter {

    public static final String SESSION_KEY = "POST_LOGIN_REDIRECT_URI";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String redirect = request.getParameter("redirect");
        if (redirect != null) {
            request.getSession().setAttribute(SESSION_KEY, redirect);
        }
        filterChain.doFilter(request, response);
    }
}
