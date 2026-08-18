package com.thevip.music;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class MusicStreamingLinkAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final long TEST_PLATFORM_ID = 555001L;

    @Test
    void MUSIC_ADMIN은_플랫폼_전체_링크를_한번에_등록하고_다시_요청하면_교체된다() throws Exception {
        mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/" + TEST_PLATFORM_ID)
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "osGroups": [
                                    {
                                      "os": "ANDROID",
                                      "links": [
                                        {"label": "멜론 링크 1", "url": "melonapp://streaming"},
                                        {"label": "멜론 링크 2", "url": "melonapp://streaming"}
                                      ]
                                    },
                                    {
                                      "os": "IPHONE",
                                      "links": [
                                        {"label": "멜론 링크 1", "url": "melonapp://streaming"}
                                      ]
                                    }
                                  ]
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].os").value("ANDROID"))
                .andExpect(jsonPath("$.data[0].label").value("멜론 링크 1"))
                .andExpect(jsonPath("$.data[0].sortOrder").value(0))
                .andExpect(jsonPath("$.data[1].sortOrder").value(1))
                .andExpect(jsonPath("$.data[2].os").value("IPHONE"));

        mockMvc.perform(get("/api/v1/admin/music/streaming-links").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isOk());

        // 같은 플랫폼으로 다시 요청하면 기존 링크는 전부 지워지고 새 목록으로 교체된다.
        mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/" + TEST_PLATFORM_ID)
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "osGroups": [
                                    {"os": "ANDROID", "links": [{"label": "새 링크", "url": "melonapp://new"}]}
                                  ]
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].label").value("새 링크"));
    }

    @Test
    void VOTE_ADMIN이나_일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/music/streaming-links").with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/music/streaming-links").with(loginAs("USER")))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor loginAs(String role) {
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
                List.of(new SimpleGrantedAuthority("ROLE_" + role)),
                Map.of("id", 1L),
                "id");
        return SecurityMockMvcRequestPostProcessors.oauth2Login()
                .clientRegistration(kakao)
                .oauth2User(principal);
    }
}
