package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
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

    @Autowired
    PlatformRepository platformRepository;

    @Test
    void MUSIC_ADMIN은_플랫폼_전체_링크를_한번에_등록하고_다시_요청하면_교체된다() throws Exception {
        Long platformId = platformRepository.save(
                Platform.of("멜론", "melon-test", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null)).getId();

        mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/" + platformId)
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
                .andExpect(jsonPath("$.data.platformId").value(platformId))
                .andExpect(jsonPath("$.data.name").value("멜론"))
                .andExpect(jsonPath("$.data.region").value("DOMESTIC"))
                .andExpect(jsonPath("$.data.osGroups.length()").value(2))
                .andExpect(jsonPath("$.data.osGroups[0].os").value("ANDROID"))
                .andExpect(jsonPath("$.data.osGroups[0].links.length()").value(2))
                .andExpect(jsonPath("$.data.osGroups[0].links[0].label").value("멜론 링크 1"))
                .andExpect(jsonPath("$.data.osGroups[1].os").value("IPHONE"))
                .andExpect(jsonPath("$.data.osGroups[1].links.length()").value(1));

        mockMvc.perform(get("/api/v1/admin/music/streaming-links").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isOk());

        // 같은 플랫폼으로 다시 요청하면 기존 링크는 전부 지워지고 새 목록으로 교체된다.
        mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/" + platformId)
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
                .andExpect(jsonPath("$.data.osGroups.length()").value(1))
                .andExpect(jsonPath("$.data.osGroups[0].links[0].label").value("새 링크"));
    }

    @Test
    void active를_생략하면_true가_기본값이고_명시하면_그대로_반영된다() throws Exception {
        Long platformId = platformRepository.save(
                Platform.of("지니", "genie-test", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null)).getId();

        String created = mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/" + platformId)
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "osGroups": [
                                    {
                                      "os": "ANDROID",
                                      "links": [
                                        {"label": "생략됨", "url": "geniemusic://a"},
                                        {"label": "명시적으로 꺼짐", "url": "geniemusic://b", "active": false}
                                      ]
                                    }
                                  ]
                                }"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> links = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.osGroups[0].links");
        assertThat(links).hasSize(2);

        String listJson = mockMvc.perform(get("/api/v1/admin/music/streaming-links").with(loginAs("MUSIC_ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> allLinks = com.jayway.jsonpath.JsonPath.parse(listJson)
                .read("$.data[?(@.platformId == " + platformId + ")]");
        assertThat(allLinks).hasSize(2);
        assertThat(allLinks.stream().filter(l -> l.get("label").equals("생략됨")).findFirst().orElseThrow().get("active"))
                .isEqualTo(true);
        assertThat(allLinks.stream().filter(l -> l.get("label").equals("명시적으로 꺼짐")).findFirst().orElseThrow()
                .get("active")).isEqualTo(false);
    }

    @Test
    void 존재하지_않는_플랫폼이면_404() throws Exception {
        mockMvc.perform(put("/api/v1/admin/music/streaming-links/platforms/999999")
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "osGroups": [
                                    {"os": "ANDROID", "links": [{"label": "링크", "url": "melonapp://x"}]}
                                  ]
                                }"""))
                .andExpect(status().isNotFound());
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
