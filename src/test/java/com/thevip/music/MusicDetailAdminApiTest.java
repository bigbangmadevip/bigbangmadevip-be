package com.thevip.music;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class MusicDetailAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String REQUEST_BODY = """
            {
              "category": "STREAMING",
              "title": "어드민 생성 테스트",
              "songName": "테스트곡",
              "platformCodes": [],
              "checklist": [],
              "imageUrls": [],
              "guideIds": [],
              "menuUrgent": false,
              "active": true
            }""";

    @Test
    void MUSIC_ADMIN은_생성_조회_수정을_할_수_있다() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/music/details")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("어드민 생성 테스트"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/music/details/" + id).with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.songName").value("테스트곡"));

        mockMvc.perform(get("/api/v1/admin/music/details").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isOk());

        String updateBody = """
                {
                  "category": "STREAMING",
                  "title": "수정된 제목",
                  "platformCodes": [],
                  "checklist": [],
                  "imageUrls": [],
                  "guideIds": [],
                  "menuUrgent": false,
                  "active": false
                }""";
        mockMvc.perform(put("/api/v1/admin/music/details/" + id)
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void platformCodes로_등록하면_응답에_같은_code로_돌아온다() throws Exception {
        String body = """
                {
                  "category": "STREAMING",
                  "title": "플랫폼 코드 테스트",
                  "platformCodes": ["melon", "bugs"],
                  "checklist": [],
                  "imageUrls": [],
                  "guideIds": [],
                  "menuUrgent": false,
                  "active": true
                }""";

        mockMvc.perform(post("/api/v1/admin/music/details")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platformCodes.length()").value(2))
                .andExpect(jsonPath("$.data.platformCodes[0]").value("melon"))
                .andExpect(jsonPath("$.data.platformCodes[1]").value("bugs"));
    }

    @Test
    void 존재하지_않는_platformCode면_404() throws Exception {
        String body = """
                {
                  "category": "STREAMING",
                  "title": "잘못된 코드 테스트",
                  "platformCodes": ["not-a-real-platform"],
                  "checklist": [],
                  "imageUrls": [],
                  "guideIds": [],
                  "menuUrgent": false,
                  "active": true
                }""";

        mockMvc.perform(post("/api/v1/admin/music/details")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    void VOTE_ADMIN이나_일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/music/details").with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));

        mockMvc.perform(get("/api/v1/admin/music/details").with(loginAs("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));
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
