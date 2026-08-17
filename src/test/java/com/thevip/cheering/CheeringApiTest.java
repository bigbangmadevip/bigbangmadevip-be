package com.thevip.cheering;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
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
 * CSRF 자체(누락/오타 토큰 403 등)는 CsrfFlowTest가 이미 이 컨트롤러의 엔드포인트로 검증하므로
 * 여기서는 중복하지 않고, 참여 로직(성공/중복/존재하지 않음/미인증)만 다룬다.
 * .with(csrf())(SecurityMockMvcRequestPostProcessors 숏컷)를 안 쓰는 이유: com.thevip.cheering 패키지가
 * 알파벳 순으로 com.thevip.global보다 먼저 실행되는 유일한 패키지라 여기서 그 숏컷을 쓰면
 * CsrfFlowTest(실제 쿠키 왕복으로 CSRF를 검증)의 XSRF-TOKEN 쿠키 발급이 깨진다 (같은 컨텍스트를
 * 공유하는 SpringBootTest 간 상태 오염, Spring Security 7.1의 지연 토큰 로딩 관련 이슈로 추정).
 * 대신 CsrfFlowTest와 동일하게, 틀린 토큰으로 한 번 찔러 실제 XSRF-TOKEN 쿠키를 받은 뒤
 * 그 값을 그대로 실어 재요청하는 방식만 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheeringApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Test
    void 응원에_참여하면_200과_참여_현황을_반환한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80001", "참여테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        RequestPostProcessor login = loginAs("80001");

        Cookie xsrfCookie = fetchRealCsrfCookie("/api/v1/cheerings/" + itemId, login);
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.typeCompletedCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void 같은_항목에_다시_참여하면_409가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80002", "중복참여테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        RequestPostProcessor login = loginAs("80002");

        Cookie xsrfCookie = fetchRealCsrfCookie("/api/v1/cheerings/" + itemId, login);
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CH001"));
    }

    @Test
    void 존재하지_않는_항목에_참여하면_404가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80003", "존재안함테스트");
        RequestPostProcessor login = loginAs("80003");

        Cookie xsrfCookie = fetchRealCsrfCookie("/api/v1/cheerings/999999", login);
        mockMvc.perform(post("/api/v1/cheerings/999999")
                        .with(login)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    void 로그인_안하면_401이_반환된다() throws Exception {
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();

        Cookie xsrfCookie = fetchRealCsrfCookie("/api/v1/cheerings/" + itemId, null);
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("C002"));
    }

    // CSRF 필터가 인증 필터보다 먼저 실행돼서, 일부러 틀린 토큰으로 한 번 찔러 응답에 실린
    // 실제 XSRF-TOKEN 쿠키를 받아낸다 (CsrfFlowTest와 동일 패턴). login이 null이면 미인증 상태로 찌른다.
    private Cookie fetchRealCsrfCookie(String url, RequestPostProcessor login) throws Exception {
        var request = post(url).header("X-XSRF-TOKEN", "wrong-value");
        if (login != null) {
            request.with(login);
        }
        MockHttpServletResponse response = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        return response.getCookie("XSRF-TOKEN");
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
