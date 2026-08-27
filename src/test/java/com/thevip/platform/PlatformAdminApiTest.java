package com.thevip.platform;

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
class PlatformAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String REQUEST_BODY = """
            {
              "name": "어드민 플랫폼 생성 테스트",
              "code": "test-platform",
              "type": "MUSIC",
              "region": "DOMESTIC",
              "active": true
            }""";

    @Test
    void MUSIC_ADMIN과_VOTE_ADMIN_둘_다_생성_조회_수정할_수_있다() throws Exception {
        String created = mockMvc.perform(post("/api/v1/admin/platforms")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("어드민 플랫폼 생성 테스트"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/platforms/" + id).with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/platforms/" + id)
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정된 플랫폼",
                                  "code": "test-platform",
                                  "type": "VOTE",
                                  "region": "GLOBAL",
                                  "active": false
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 플랫폼"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void 이미_사용중인_code로_생성하면_409() throws Exception {
        mockMvc.perform(post("/api/v1/admin/platforms")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "첫 번째 플랫폼",
                                  "code": "duplicate-code",
                                  "type": "MUSIC",
                                  "region": "DOMESTIC",
                                  "active": true
                                }"""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/platforms")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "두 번째 플랫폼",
                                  "code": "duplicate-code",
                                  "type": "MUSIC",
                                  "region": "DOMESTIC",
                                  "active": true
                                }"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("P001"));
    }

    @Test
    void 일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/platforms").with(loginAs("USER")))
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
