package com.thevip.global.config;

import com.thevip.global.security.CsrfCookieFilter;
import com.thevip.global.security.DynamicRedirectSuccessHandler;
import com.thevip.global.security.RedirectCaptureFilter;
import com.thevip.global.security.RestAccessDeniedHandler;
import com.thevip.global.security.RestAuthenticationEntryPoint;
import com.thevip.global.security.RestLogoutSuccessHandler;
import com.thevip.global.security.SessionRenewalFilter;
import com.thevip.member.service.CustomOAuth2UserService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.front-url}")
    private String frontUrl;

    // 실제 배포 환경(예: .bigbangmadevip.com)에서만 지정 — 로컬/ngrok처럼 그 도메인과 무관한
    // host로 접속할 때 지정돼 있으면 브라우저가 쿠키 자체를 거부해 로그인이 아예 깨진다.
    @Value("${app.cookie-domain:}")
    private String cookieDomain;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            RestLogoutSuccessHandler restLogoutSuccessHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(restLogoutSuccessHandler))
                .csrf(csrf -> {
                    CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    // 세션 쿠키(same-site: none)와 달리 이 쿠키는 명시 안 하면 브라우저 기본값(Lax)로 내려가서
                    // cross-site 쓰기 요청에 아예 안 실려간다. 세션 쿠키와 동일하게 None으로 맞춰줌.
                    csrfTokenRepository.setCookieCustomizer(cookie -> cookie.sameSite("None").secure(true));
                    csrf.csrfTokenRepository(csrfTokenRepository)
                            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
                })
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterBefore(new RedirectCaptureFilter(), OAuth2AuthorizationRequestRedirectFilter.class)
                .addFilterAfter(new SessionRenewalFilter(), CsrfCookieFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/health", "/h2-console/**", "/s/**").permitAll()
                        // 단순 조회 API는 로그인 없이도 열람 가능해야 한다. 개인화 데이터(응원 완료 여부 등)가
                        // 섞인 /api/v1/home은 비로그인 시 컨트롤러에서 빈 값으로 처리한다.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/home",
                                "/api/v1/music/streaming", "/api/v1/music/detail/**",
                                "/api/v1/music/notices", "/api/v1/music/notices/**",
                                "/api/v1/vote/today", "/api/v1/vote/detail/**",
                                "/api/v1/vote/schedules", "/api/v1/vote/schedules/**",
                                "/api/v1/vote/notices", "/api/v1/vote/notices/**",
                                "/api/v1/schedule", "/api/v1/schedule/months/**", "/api/v1/schedule/days/**")
                        .permitAll()
                        // 이 셋은 요청 목록/승인/반려의 실제 대상 제한(MASTER는 전부, MUSIC_ADMIN/VOTE_ADMIN은
                        // 자기 도메인 신청만)은 컨트롤러 진입 후 서비스 레이어에서 건별로 판단한다. 여기서는
                        // 일반 USER를 걸러내는 정도만 담당.
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/role-requests")
                        .hasAnyRole("MASTER", "MUSIC_ADMIN", "VOTE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/role-requests/*/approve")
                        .hasAnyRole("MASTER", "MUSIC_ADMIN", "VOTE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/role-requests/*/reject")
                        .hasAnyRole("MASTER", "MUSIC_ADMIN", "VOTE_ADMIN")
                        // 콘텐츠 어드민 CRUD. 도메인 전용(음원/투표)은 URL 프리픽스로 바로 막고,
                        // 공용 리소스(가이드/플랫폼/오늘의 응원)는 두 관리자 모두 접근 가능하게 둔다.
                        .requestMatchers("/api/v1/admin/music/**").hasAnyRole("MUSIC_ADMIN", "MASTER")
                        .requestMatchers("/api/v1/admin/vote/**").hasAnyRole("VOTE_ADMIN", "MASTER")
                        .requestMatchers("/api/v1/admin/guides/**", "/api/v1/admin/platforms/**",
                                "/api/v1/admin/cheering-items/**", "/api/v1/admin/images/**",
                                "/api/v1/admin/push/**")
                        .hasAnyRole("MUSIC_ADMIN", "VOTE_ADMIN", "MASTER")
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(new DynamicRedirectSuccessHandler(frontUrl)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowedOrigins = new ArrayList<>(frontUrl.equals("http://localhost:3000")
                ? List.of(frontUrl)
                : List.of(frontUrl, "http://localhost:3000"));
        // 음원/투표 관리자 서브도메인. 두 곳 다 별도 프론트 배포라 origin으로 명시해야 CORS를 통과한다.
        allowedOrigins.add("https://music.bigbangmadevip.com");
        allowedOrigins.add("https://vote.bigbangmadevip.com");
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Spring Session의 기본 세션 쿠키명(SESSION)을 그대로 두면 기존 클라이언트가 쓰던
    // JSESSIONID와 어긋나서 세션 인식이 끊긴다. 이름/속성을 기존 쿠키와 동일하게 맞춘다.
    // 이 빈을 직접 정의하면 server.servlet.session.cookie.* 프로퍼티 기반 자동 설정은 적용되지 않는다.
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true);
        serializer.setSameSite("None");
        serializer.setCookieMaxAge((int) Duration.ofDays(60).toSeconds());
        if (!cookieDomain.isBlank()) {
            serializer.setDomainName(cookieDomain);
        }
        return serializer;
    }
}
