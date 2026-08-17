package com.thevip.cheering;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * com.thevip.cheering 패키지가 알파벳 순으로 com.thevip.global보다 먼저 실행되는 유일한 패키지라
 * .with(csrf()) 숏컷을 쓰면 CsrfFlowTest가 깨진다(CheeringApiTest 클래스 주석 참고). 여기서도
 * 동일하게 실제 쿠키 왕복 방식만 쓴다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheeringItemAdminApiTest {

    @Autowired
    MockMvc mockMvc;

    private static final String REQUEST_BODY = """
            {
              "category": "STREAMING",
              "title": "어드민 응원항목 생성 테스트",
              "subtitle": "부제목",
              "sortOrder": 0,
              "active": true
            }""";

    @Test
    void MUSIC_ADMIN과_VOTE_ADMIN_둘_다_생성_조회_수정할_수_있다() throws Exception {
        RequestPostProcessor musicAdmin = loginAs("MUSIC_ADMIN");
        Cookie csrfCookie = fetchRealCsrfCookie("/api/v1/admin/cheering-items", musicAdmin);

        String created = mockMvc.perform(post("/api/v1/admin/cheering-items")
                        .with(musicAdmin)
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("어드민 응원항목 생성 테스트"))
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(created).read("$.data.id", Long.class);

        RequestPostProcessor voteAdmin = loginAs("VOTE_ADMIN");
        mockMvc.perform(get("/api/v1/admin/cheering-items/" + id).with(voteAdmin))
                .andExpect(status().isOk());

        Cookie updateCsrfCookie = fetchRealCsrfCookie("/api/v1/admin/cheering-items", voteAdmin);
        mockMvc.perform(put("/api/v1/admin/cheering-items/" + id)
                        .with(voteAdmin)
                        .cookie(updateCsrfCookie)
                        .header("X-XSRF-TOKEN", updateCsrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "VOTE",
                                  "title": "수정된 항목",
                                  "subtitle": "수정된 부제목",
                                  "sortOrder": 1,
                                  "active": false
                                }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 항목"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void 일반_유저는_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cheering-items").with(loginAs("USER")))
                .andExpect(status().isForbidden());
    }

    private Cookie fetchRealCsrfCookie(String url, RequestPostProcessor login) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post(url)
                        .with(login)
                        .header("X-XSRF-TOKEN", "wrong-value"))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
        return response.getCookie("XSRF-TOKEN");
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
