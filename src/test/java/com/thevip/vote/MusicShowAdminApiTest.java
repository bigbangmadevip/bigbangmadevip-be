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
class MusicShowAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 방송과_라운드를_생성_조회_수정할_수_있다() throws Exception {
        String showBody = """
                {
                  "name": "어드민 테스트 방송",
                  "platformIds": [],
                  "active": true,
                  "sortOrder": 0,
                  "guideIds": []
                }""";
        String showResponse = mockMvc.perform(post("/api/v1/admin/vote/shows")
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(showBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("어드민 테스트 방송"))
                .andReturn().getResponse().getContentAsString();
        Long showId = com.jayway.jsonpath.JsonPath.parse(showResponse).read("$.data.id", Long.class);

        String roundBody = """
                {
                  "label": "사전 투표 1",
                  "time": "8/12(화) 10:00",
                  "tone": "advance",
                  "active": true,
                  "sortOrder": 0,
                  "rows": [{ "label": "뮤빗", "value": "바로가기" }]
                }""";
        String roundResponse = mockMvc.perform(post("/api/v1/admin/vote/shows/" + showId + "/rounds")
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roundBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.label").value("사전 투표 1"))
                .andExpect(jsonPath("$.data.rows[0].label").value("뮤빗"))
                .andReturn().getResponse().getContentAsString();
        Long roundId = com.jayway.jsonpath.JsonPath.parse(roundResponse).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/vote/shows/" + showId + "/rounds").with(loginAs("VOTE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        String updateRoundBody = """
                {
                  "label": "사전 투표 1 (수정)",
                  "time": "8/12(화) 10:00",
                  "tone": "advance",
                  "active": true,
                  "sortOrder": 0,
                  "rows": []
                }""";
        mockMvc.perform(put("/api/v1/admin/vote/shows/" + showId + "/rounds/" + roundId)
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRoundBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.label").value("사전 투표 1 (수정)"));
    }

    @Test
    void 다른_방송_소속_라운드_id로_수정하면_404다() throws Exception {
        Long showA = createShow("방송A");
        Long showB = createShow("방송B");
        Long roundOfA = createRound(showA);

        String body = """
                {
                  "label": "무관한 수정",
                  "active": true,
                  "sortOrder": 0,
                  "rows": []
                }""";
        mockMvc.perform(put("/api/v1/admin/vote/shows/" + showB + "/rounds/" + roundOfA)
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    void MUSIC_ADMIN이나_일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/vote/shows").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/vote/shows").with(loginAs("USER")))
                .andExpect(status().isForbidden());
    }

    private Long createShow(String name) throws Exception {
        String body = """
                { "name": "%s", "platformIds": [], "active": true, "sortOrder": 0, "guideIds": [] }
                """.formatted(name);
        String response = mockMvc.perform(post("/api/v1/admin/vote/shows")
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.parse(response).read("$.data.id", Long.class);
    }

    private Long createRound(Long showId) throws Exception {
        String body = """
                { "label": "라운드", "active": true, "sortOrder": 0, "rows": [] }
                """;
        String response = mockMvc.perform(post("/api/v1/admin/vote/shows/" + showId + "/rounds")
                        .with(loginAs("VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.parse(response).read("$.data.id", Long.class);
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
