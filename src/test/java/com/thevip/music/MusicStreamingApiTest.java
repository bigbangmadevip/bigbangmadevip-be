package com.thevip.music;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
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
class MusicStreamingApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Test
    void 원클릭_스트리밍은_플랫폼별_운영체제별_링크_구조로_내려준다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "40001", "스트리밍테스트");

        mockMvc.perform(get("/api/v1/music/streaming").with(loginAs("40001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platforms.length()").value(2))
                .andExpect(jsonPath("$.data.platforms[0].name").value("멜론"))
                .andExpect(jsonPath("$.data.platforms[0].region").value("DOMESTIC"))
                .andExpect(jsonPath("$.data.platforms[0].osGroups.length()").value(3))
                .andExpect(jsonPath("$.data.platforms[0].osGroups[0].os").value("ANDROID"))
                .andExpect(jsonPath("$.data.platforms[0].osGroups[0].links.length()").value(2))
                .andExpect(jsonPath("$.data.platforms[0].osGroups[0].links[0].label").value("멜론 앱으로 스트리밍"))
                .andExpect(jsonPath("$.data.platforms[1].name").value("지니"))
                .andExpect(jsonPath("$.data.platforms[1].osGroups.length()").value(2));
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
