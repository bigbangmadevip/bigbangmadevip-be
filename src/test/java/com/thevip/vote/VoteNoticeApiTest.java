package com.thevip.vote;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.repository.NoticeRepository;
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
class VoteNoticeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    NoticeRepository noticeRepository;

    @Test
    void 투표_공지_목록을_조회한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "70001", "공지목록테스트");

        mockMvc.perform(get("/api/v1/vote/notices").with(loginAs("70001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("투표 총공팀 공지사항입니다."));
    }

    @Test
    void 투표_공지_상세를_조회한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "70002", "공지상세테스트");
        Long noticeId = noticeRepository.findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(NoticeMenuType.VOTE)
                .get(0).getId();

        mockMvc.perform(get("/api/v1/vote/notices/" + noticeId).with(loginAs("70002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("투표 총공팀 공지사항입니다."))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(0));
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
