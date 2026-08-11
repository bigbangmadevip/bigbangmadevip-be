package com.thevip.mypage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.member.entity.Provider;
import com.thevip.member.service.MemberService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
class MyPageApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Test
    void 마이페이지_캘린더_기록상세는_오늘_참여한_내역을_반영한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "90001", "마이페이지테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        LocalDate today = LocalDate.now();

        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(loginAs("90001")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/mypage").with(loginAs("90001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCheering.completedCount").value(1))
                .andExpect(jsonPath("$.data.todayCheering.totalCount").value(8))
                .andExpect(jsonPath("$.data.cheeringRecord.totalParticipationCount").value(1))
                .andExpect(jsonPath("$.data.cheeringRecord.participatedDayCount").value(1))
                .andExpect(jsonPath("$.data.cheeringRecord.participatedDayCountThisMonth").value(1))
                .andExpect(jsonPath("$.data.cheeringRecord.firstParticipatedDate").value(today.toString()));

        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        mockMvc.perform(get("/api/v1/mypage/cheering-calendar/" + yearMonth).with(loginAs("90001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participatedDates.length()").value(1))
                .andExpect(jsonPath("$.data.participatedDates[0]").value(today.toString()));

        mockMvc.perform(get("/api/v1/mypage/cheering-records/" + today).with(loginAs("90001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("음원\n스트리밍"));
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
