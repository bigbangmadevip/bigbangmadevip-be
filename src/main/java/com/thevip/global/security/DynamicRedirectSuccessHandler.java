package com.thevip.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

/**
 * 세션에 저장된 redirect 값이 허용 목록에 있으면 그쪽으로, 아니면 운영 프론트(defaultUrl)로 보낸다.
 * 허용 목록 없이 아무 URL이나 리다이렉트하면 오픈 리다이렉트 취약점이 되므로 화이트리스트 검증이 필수.
 */
public class DynamicRedirectSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Set<String> ALLOWED_REDIRECT_URLS = Set.of("http://localhost:3000");

    private final String defaultUrl;

    public DynamicRedirectSuccessHandler(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        Object redirect = request.getSession().getAttribute(RedirectCaptureFilter.SESSION_KEY);
        if (redirect instanceof String url && ALLOWED_REDIRECT_URLS.contains(url)) {
            return url;
        }
        return defaultUrl;
    }
}
