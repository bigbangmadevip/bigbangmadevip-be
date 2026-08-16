package com.thevip.music;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import com.thevip.notice.entity.Notice;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class MusicNoticeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    NoticeRepository noticeRepository;

    @Test
    void 음원_공지_목록을_조회한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "60001", "공지목록테스트");

        mockMvc.perform(get("/api/v1/music/notices").with(loginAs("60001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("[수정] 스트리밍 리스트 ver.2로 업데이트 됐어요."));
    }

    @Test
    void 음원_공지_상세를_조회한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "60002", "공지상세테스트");
        Long noticeId = noticeRepository.findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(NoticeMenuType.MUSIC)
                .get(0).getId();

        mockMvc.perform(get("/api/v1/music/notices/" + noticeId).with(loginAs("60002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("[수정] 스트리밍 리스트 ver.2로 업데이트 됐어요."))
                .andExpect(jsonPath("$.data.imageUrls.length()").value(1));
    }

    // 다른 테스트의 "공지 1개" 같은 개수 검증을 오염시키지 않도록, 여기서 추가한 공지는 테스트 종료 후 롤백한다.
    @Test
    @Transactional
    void 고정된_공지가_최신_공지보다_먼저_노출된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "60003", "고정공지테스트");
        Notice pinned = Notice.of(NoticeMenuType.MUSIC, "고정된 공지", "내용");
        pinned.updatePinned(true);
        noticeRepository.save(pinned);
        // 시드 공지("[수정] 스트리밍 리스트 ver.2로...")보다 createdAt이 더 최신인 일반 공지를 하나 더 추가해도
        // 고정 공지가 여전히 맨 위에 와야 한다.
        noticeRepository.save(Notice.of(NoticeMenuType.MUSIC, "그냥 최신 공지", "내용"));

        mockMvc.perform(get("/api/v1/music/notices").with(loginAs("60003")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("고정된 공지"))
                .andExpect(jsonPath("$.data[0].pinned").value(true))
                .andExpect(jsonPath("$.data[1].title").value("그냥 최신 공지"))
                .andExpect(jsonPath("$.data[1].pinned").value(false));
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
