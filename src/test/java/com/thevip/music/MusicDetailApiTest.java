package com.thevip.music;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import com.thevip.music.repository.MusicDetailRepository;
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
class MusicDetailApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MusicDetailRepository musicDetailRepository;

    @Test
    void 음원_상세를_조회하면_플랫폼명과_체크리스트를_내려준다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "50001", "상세테스트");
        Long detailId = musicDetailRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/music/detail/" + detailId).with(loginAs("50001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("오늘 저녁 8시 30분 멜론 개별곡 다운로드 총공"))
                .andExpect(jsonPath("$.data.songName").value("타이틀 곡 <봄여름가을겨울>"))
                .andExpect(jsonPath("$.data.platformNames.length()").value(2))
                .andExpect(jsonPath("$.data.platformNames[0]").value("멜론"))
                .andExpect(jsonPath("$.data.platformNames[1]").value("벅스(Bugs)"))
                .andExpect(jsonPath("$.data.checklist.length()").value(2));
    }

    @Test
    void 로그인하지_않아도_조회할_수_있다() throws Exception {
        Long detailId = musicDetailRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/v1/music/detail/" + detailId))
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_상세를_조회하면_404가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "50002", "존재안함테스트");

        mockMvc.perform(get("/api/v1/music/detail/999999").with(loginAs("50002")))
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
