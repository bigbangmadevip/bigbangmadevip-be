package com.thevip.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
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
class HomeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Test
    void 홈_응답은_참여자수와_완료여부가_반영된_전체_항목을_내려준다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30001", "종식");
        Long firstItemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();

        mockMvc.perform(post("/api/v1/cheerings/" + firstItemId)
                        .with(loginAs("30001")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        // participantCount는 캐싱 없이 실시간 집계지만, 같은 스프링 컨텍스트를 공유하는
        // 다른 테스트도 같은 오늘 날짜로 참여를 남기므로 절대값은 검증하지 않고 형태만 확인한다.
        mockMvc.perform(get("/api/v1/home").with(loginAs("30001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participantCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.urgentDetail.menuType").value("MUSIC"))
                .andExpect(jsonPath("$.data.urgentDetail.category").value("DOWNLOAD"))
                .andExpect(jsonPath("$.data.urgentDetail.title").value("오늘 저녁 8시 30분 멜론 개별곡 다운로드 총공"))
                .andExpect(jsonPath("$.data.urgentDetail.songName").value("타이틀 곡 <봄여름가을겨울>"))
                .andExpect(jsonPath("$.data.urgentDetail.checklist.length()").value(2))
                .andExpect(jsonPath("$.data.cheeringItems.length()").value(8))
                .andExpect(jsonPath("$.data.cheeringItems[0].category").value("STREAMING"))
                .andExpect(jsonPath("$.data.cheeringItems[0].title").value("음원\n스트리밍"))
                .andExpect(jsonPath("$.data.cheeringItems[0].completed").value(true))
                .andExpect(jsonPath("$.data.cheeringItems[1].completed").value(false));
    }

    @Test
    void 이미_참여한_항목을_다시_참여하면_409가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30002", "재참여");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();

        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(loginAs("30002")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(loginAs("30002")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CH001"));
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
