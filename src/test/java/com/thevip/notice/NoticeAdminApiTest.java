package com.thevip.notice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    private static final String REQUEST_BODY = """
            {
              "title": "어드민 공지 생성 테스트",
              "content": "본문",
              "imageUrls": [],
              "links": [{"label": "이벤트 페이지", "url": "https://example.com/event"}],
              "pinned": false,
              "active": true
            }""";

    // 생성한 공지가 커밋되면 목록 개수를 정확히 세는 다른 공지 테스트(MusicNoticeApiTest 등)를 오염시켜서
    // 롤백되도록 트랜잭션으로 감싼다.
    @Transactional
    @Test
    void MUSIC_ADMIN은_음원_공지를_생성_조회_수정할_수_있다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "41001", "음원공지어드민");

        String created = mockMvc.perform(post("/api/v1/admin/music/notices")
                        .with(loginAs("41001", "MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuType").value("MUSIC"))
                .andExpect(jsonPath("$.data.updatedBy").value("음원공지어드민"))
                .andExpect(jsonPath("$.data.links.length()").value(1))
                .andExpect(jsonPath("$.data.links[0].label").value("이벤트 페이지"))
                .andExpect(jsonPath("$.data.links[0].url").value("https://example.com/event"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/music/notices/" + id).with(loginAs("41001", "MUSIC_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("어드민 공지 생성 테스트"));

        mockMvc.perform(put("/api/v1/admin/music/notices/" + id)
                        .with(loginAs("41001", "MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정된 공지",
                                  "content": "수정된 본문",
                                  "imageUrls": [],
                                  "links": [],
                                  "pinned": true,
                                  "active": true
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 공지"))
                .andExpect(jsonPath("$.data.pinned").value(true))
                .andExpect(jsonPath("$.data.links.length()").value(0));

        // 음원 공지 id로 투표 쪽 URL을 조회하면 404
        mockMvc.perform(get("/api/v1/admin/vote/notices/" + id).with(loginAs("41005", "VOTE_ADMIN")))
                .andExpect(status().isNotFound());
    }

    // 생성한 공지가 커밋되면 목록 개수를 정확히 세는 다른 공지 테스트(VoteNoticeApiTest 등)를 오염시켜서
    // 롤백되도록 트랜잭션으로 감싼다.
    @Transactional
    @Test
    void VOTE_ADMIN은_투표_공지를_생성할_수_있다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "41002", "투표공지어드민");

        mockMvc.perform(post("/api/v1/admin/vote/notices")
                        .with(loginAs("41002", "VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menuType").value("VOTE"));
    }

    @Test
    void 다른_도메인_관리자는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/music/notices").with(loginAs("41003", "VOTE_ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/vote/notices").with(loginAs("41004", "MUSIC_ADMIN")))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor loginAs(String kakaoId, String role) {
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
                Map.of("id", Long.parseLong(kakaoId)),
                "id");
        return SecurityMockMvcRequestPostProcessors.oauth2Login()
                .clientRegistration(kakao)
                .oauth2User(principal);
    }
}
