package com.thevip.vote;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class VoteDetailApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    VoteDetailRepository voteDetailRepository;

    @Test
    void 투표_상세를_조회하면_플랫폼명과_CTA_정보를_내려준다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "60001", "투표상세테스트");
        Long detailId = voteDetailRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/vote/detail/" + detailId).with(loginAs("60001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("인기가요 생방송 투표"))
                .andExpect(jsonPath("$.data.platformNames").value(org.hamcrest.Matchers.contains("하이어(Higher)")))
                .andExpect(jsonPath("$.data.platformUrl").value("https://example.com/vote"))
                .andExpect(jsonPath("$.data.ctaButtonLabel").value("투표하러 가기"))
                .andExpect(jsonPath("$.data.checklist.length()").value(1));
    }

    @Test
    void 로그인하지_않아도_조회할_수_있다() throws Exception {
        Long detailId = voteDetailRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/vote/detail/" + detailId))
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_상세를_조회하면_404가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "60002", "존재안함테스트");

        mockMvc.perform(get("/api/v1/vote/detail/999999").with(loginAs("60002")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C004"));
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
