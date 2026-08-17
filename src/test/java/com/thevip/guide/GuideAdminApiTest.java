package com.thevip.guide;

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
class GuideAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String REQUEST_BODY = """
            {
              "guideType": "STREAMING",
              "title": "어드민 가이드 생성 테스트",
              "imageUrls": [],
              "active": true,
              "sortOrder": 0
            }""";

    @Test
    void MUSIC_ADMIN과_VOTE_ADMIN_둘_다_생성_조회_수정할_수_있다() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/guides")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("어드민 가이드 생성 테스트"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/guides/" + id).with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/guides/" + id)
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "guideType": "DOWNLOAD",
                                  "title": "수정된 가이드",
                                  "imageUrls": [],
                                  "active": false,
                                  "sortOrder": 1
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.guideType").value("DOWNLOAD"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void 일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/guides").with(loginAs("USER")))
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
