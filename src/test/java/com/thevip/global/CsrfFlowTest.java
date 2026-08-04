package com.thevip.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * .with(csrf())는 토큰 매칭을 우회해서 실제 쿠키<->헤더 왕복 버그(Xor 마스킹 핸들러 이슈)를 잡지 못한다.
 * 여기서는 브라우저/Postman이 하는 것과 똑같이: 서버가 내려준 쿠키 원본 값을 그대로 헤더에 실어 보내 검증한다.
 *
 * CSRF 자체가 SecurityConfig에서 임시로 꺼져있는 동안은(2026-08, cross-origin 쿠키 문제) 이 검증이 의미가 없어
 * 통째로 비활성화해둔다. CSRF 재활성화 시 @Disabled만 지우면 그대로 다시 쓸 수 있다.
 */
@Disabled("CSRF가 SecurityConfig에서 임시로 꺼져있음 - 재활성화 시 이 클래스도 같이 켤 것")
@SpringBootTest
@AutoConfigureMockMvc
class CsrfFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Test
    void 쿠키의_토큰값을_그대로_헤더에_실으면_통과한다() throws Exception {
        memberService.findOrCreate(
                com.thevip.member.entity.Provider.KAKAO, "40001", "csrf테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        RequestPostProcessor login = loginAs("40001");

        // 1차: 아무 값이나 헤더에 실어 POST -> 403이지만, 서버가 실제 토큰을 비교하려고 리졸브하면서
        // XSRF-TOKEN 쿠키를 응답에 실어준다 (헤더 자체가 없으면 비교 전에 즉시 거부되어 토큰이 발급되지 않는다).
        MockHttpServletResponse firstResponse = mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .header("X-XSRF-TOKEN", "wrong-value"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();

        Cookie xsrfCookie = firstResponse.getCookie("XSRF-TOKEN");
        assertThat(xsrfCookie).isNotNull();
        String rawToken = xsrfCookie.getValue();

        // 2차: 쿠키에 찍힌 원본 값을 그대로 헤더로 되돌려보냄 -> 통과해야 정상
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", rawToken))
                .andExpect(status().isOk());
    }

    @Test
    void 로그인_후_조회만_해도_XSRF_TOKEN_쿠키가_발급된다() throws Exception {
        memberService.findOrCreate(com.thevip.member.entity.Provider.KAKAO, "40002", "쿠키선발급테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        RequestPostProcessor login = loginAs("40002");

        // 실패시키는 트릭 없이, 프론트의 실제 흐름(로그인 -> 조회)만으로 쿠키가 나와야 한다.
        MockHttpServletResponse getResponse = mockMvc.perform(get("/api/v1/me").with(login))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Cookie xsrfCookie = getResponse.getCookie("XSRF-TOKEN");
        assertThat(xsrfCookie).isNotNull();

        // 그 쿠키 값을 그대로 헤더에 실어 첫 POST 시도에서 바로 성공해야 한다 (실패 후 재시도 아님).
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk());
    }

    private RequestPostProcessor loginAs(String kakaoId) {
        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
                .clientId("test")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("id", Long.parseLong(kakaoId)),
                "id");
        return SecurityMockMvcRequestPostProcessors.oauth2Login()
                .clientRegistration(kakao)
                .oauth2User(principal);
    }
}
