package com.thevip.vote;

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
class VoteDetailAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String REQUEST_BODY = """
            {
              "category": "MUSIC_SHOW",
              "title": "어드민 생성 테스트",
              "platformIds": [],
              "checklist": [],
              "imageUrls": [],
              "guideIds": [],
              "menuUrgent": false,
              "active": true,
              "pushEnabled": false
            }""";

    @Test
    void VOTE_ADMIN은_생성_조회_수정을_할_수_있다() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/vote/details")
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("어드민 생성 테스트"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/vote/details/" + id).with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("MUSIC_SHOW"));

        mockMvc.perform(get("/api/v1/admin/vote/details").with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isOk());

        String updateBody = """
                {
                  "category": "MUSIC_SHOW",
                  "title": "수정된 제목",
                  "platformIds": [],
                  "checklist": [],
                  "imageUrls": [],
                  "guideIds": [],
                  "menuUrgent": false,
                  "active": false,
                  "pushEnabled": false
                }""";
        mockMvc.perform(put("/api/v1/admin/vote/details/" + id)
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void MUSIC_ADMIN이나_일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/vote/details").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));

        mockMvc.perform(get("/api/v1/admin/vote/details").with(loginAs("USER")))
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
