package com.thevip.global.config;

import com.thevip.global.security.RestAuthenticationEntryPoint;
import com.thevip.member.service.CustomOAuth2UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
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
            RestAuthenticationEntryPoint restAuthenticationEntryPoint) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(restAuthenticationEntryPoint))
                // TODO: 임시로 꺼둠 (2026-08). 프론트(다른 컴퓨터)와 백엔드(ngrok 도메인)가 서로 다른 origin이라
                // XSRF-TOKEN 쿠키를 프론트 JS가 애초에 읽을 수 없는 구조적 문제 때문에 CSRF 검증 자체를 비활성화.
                // 실사용자가 붙기 전에 반드시 재활성화할 것 — CookieCsrfTokenRepository 설정과
                // CsrfCookieFilter(global/security)는 그대로 남겨뒀으니 되돌릴 때 재사용하면 된다.
                // 재활성화 시 대안: CSRF 토큰을 쿠키가 아니라 API 응답 본문으로 내려주는 방식으로 전환.
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/health", "/h2-console/**", "/s/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl(frontUrl));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
