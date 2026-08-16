package com.thevip.global.config;

import com.thevip.global.security.CsrfCookieFilter;
import com.thevip.global.security.DynamicRedirectSuccessHandler;
import com.thevip.global.security.RedirectCaptureFilter;
import com.thevip.global.security.RestAuthenticationEntryPoint;
import com.thevip.global.security.RestLogoutSuccessHandler;
import com.thevip.member.service.CustomOAuth2UserService;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CustomOAuth2UserService customOAuth2UserService,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestLogoutSuccessHandler restLogoutSuccessHandler) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(restAuthenticationEntryPoint))
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
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/health", "/h2-console/**", "/s/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(new DynamicRedirectSuccessHandler(frontUrl)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(frontUrl.equals("http://localhost:3000")
                ? List.of(frontUrl)
                : List.of(frontUrl, "http://localhost:3000"));
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
        return serializer;
    }
}
