package com.thevip.schedule;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class ScheduleApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Test
    void 월간_일정은_음원_시드와_투표_시드가_등록된_오늘_날짜에_카운트를_반환한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80001", "일정테스트");
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        mockMvc.perform(get("/api/v1/schedule/months/" + yearMonth).with(loginAs("80001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.days.length()").value(1))
                .andExpect(jsonPath("$.data.days[0].date").value(today.toString()))
                .andExpect(jsonPath("$.data.days[0].musicCount").value(1))
                .andExpect(jsonPath("$.data.days[0].voteCount").value(1));
    }

    @Test
    void 일별_일정은_시간순으로_음원_투표를_함께_반환한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80002", "일정테스트2");
        LocalDate today = LocalDate.now();

        mockMvc.perform(get("/api/v1/schedule/days/" + today).with(loginAs("80002")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].menuType").value("MUSIC"))
                .andExpect(jsonPath("$.data.items[1].menuType").value("VOTE"));
    }

    @Test
    void 초기_진입_조회는_파라미터_생략시_이번달_오늘_기준으로_월과_일을_함께_반환한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "80003", "일정테스트3");
        LocalDate today = LocalDate.now();

        mockMvc.perform(get("/api/v1/schedule").with(loginAs("80003")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month.days.length()").value(1))
                .andExpect(jsonPath("$.data.month.days[0].date").value(today.toString()))
                .andExpect(jsonPath("$.data.day.date").value(today.toString()))
                .andExpect(jsonPath("$.data.day.items.length()").value(2));
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
