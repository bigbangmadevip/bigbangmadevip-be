package com.thevip.vote;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import com.thevip.vote.repository.MusicShowRepository;
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
class VoteScheduleApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MusicShowRepository musicShowRepository;

    @Test
    void 투표_플랜_목록을_조회하면_방송_목록을_반환한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "70001", "투표플랜목록테스트");

        mockMvc.perform(get("/api/v1/vote/schedules").with(loginAs("70001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("쇼! 음악중심"));
    }

    @Test
    void 투표_플랜_상세를_조회하면_라운드_정보를_내려준다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "70002", "투표플랜상세테스트");
        Long showId = musicShowRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/vote/schedules/" + showId).with(loginAs("70002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("쇼! 음악중심"))
                .andExpect(jsonPath("$.data.channel").value("MBC"))
                .andExpect(jsonPath("$.data.rounds.length()").value(4))
                .andExpect(jsonPath("$.data.rounds[0].label").value("사전 투표 1"))
                .andExpect(jsonPath("$.data.rounds[0].rows.length()").value(2));
    }

    @Test
    void 존재하지_않는_투표_플랜을_조회하면_404가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "70003", "투표플랜존재안함테스트");

        mockMvc.perform(get("/api/v1/vote/schedules/999999").with(loginAs("70003")))
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
