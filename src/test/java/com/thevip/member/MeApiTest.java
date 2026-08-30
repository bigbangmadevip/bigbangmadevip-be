package com.thevip.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.cheering.repository.CheeringItemRepository;
import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.member.entity.Member;
import com.thevip.member.entity.Provider;
import com.thevip.member.repository.MemberRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class MeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CheeringItemRepository cheeringItemRepository;

    @Autowired
    CheeringRepository cheeringRepository;

    @Test
    void 로그인한_회원의_정보를_조회한다() throws Exception {
        Member member = memberService.findOrCreate(Provider.KAKAO, "20001", "종식");

        mockMvc.perform(get("/api/v1/me").with(loginAs("20001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.name").value("종식"))
                .andExpect(jsonPath("$.data.nickname").value("종식"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.termsAgreed").value(false))
                .andExpect(jsonPath("$.data.urgentPushEnabled").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.musicPushEnabled").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data.votePushEnabled").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void 알림_설정값이_DB에서_null이어도_조회가_깨지지_않는다() throws Exception {
        Member member = memberService.findOrCreate(Provider.KAKAO, "20010", "널테스트");
        ReflectionTestUtils.setField(member, "musicPushEnabled", null);
        memberRepository.saveAndFlush(member);

        mockMvc.perform(get("/api/v1/me").with(loginAs("20010")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.musicPushEnabled").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void 약관에_동의하면_이후_조회에_반영된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20004", "약관테스트");

        mockMvc.perform(post("/api/v1/me/terms-agreement")
                        .with(loginAs("20004")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.termsAgreed").value(true));

        mockMvc.perform(get("/api/v1/me").with(loginAs("20004")))
                .andExpect(jsonPath("$.data.termsAgreed").value(true));
    }

    @Test
    void 닉네임을_수정하면_이후_조회에_반영된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20002", "카카오닉네임");

        mockMvc.perform(patch("/api/v1/me")
                        .with(loginAs("20002"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"내가정한별명\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("내가정한별명"))
                .andExpect(jsonPath("$.data.name").value("카카오닉네임"));

        mockMvc.perform(get("/api/v1/me").with(loginAs("20002")))
                .andExpect(jsonPath("$.data.nickname").value("내가정한별명"));
    }

    @Test
    void fcm_토큰을_등록하면_저장된다() throws Exception {
        Member member = memberService.findOrCreate(Provider.KAKAO, "20007", "푸시테스트");

        mockMvc.perform(patch("/api/v1/me/fcm-token")
                        .with(loginAs("20007"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"test-token-value\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getFcmToken()).isEqualTo("test-token-value");
    }

    @Test
    void 알림_설정을_저장하면_이후_조회에_반영된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20009", "알림설정테스트");

        mockMvc.perform(patch("/api/v1/me/push-settings")
                        .with(loginAs("20009"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"urgentPushEnabled\":true,\"musicPushEnabled\":true,\"votePushEnabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.urgentPushEnabled").value(true))
                .andExpect(jsonPath("$.data.musicPushEnabled").value(true))
                .andExpect(jsonPath("$.data.votePushEnabled").value(false));

        mockMvc.perform(get("/api/v1/me").with(loginAs("20009")))
                .andExpect(jsonPath("$.data.urgentPushEnabled").value(true))
                .andExpect(jsonPath("$.data.musicPushEnabled").value(true))
                .andExpect(jsonPath("$.data.votePushEnabled").value(false));
    }

    @Test
    void 빈_fcm_토큰으로_등록하면_400이_내려간다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20008", "푸시빈값테스트");

        mockMvc.perform(patch("/api/v1/me/fcm-token")
                        .with(loginAs("20008"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fcmToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    void 빈_닉네임으로_수정하면_400이_내려간다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20003", "카카오닉네임");

        mockMvc.perform(patch("/api/v1/me")
                        .with(loginAs("20003"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    void 로그인_안하면_401과_에러_JSON이_내려간다() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C002"));
    }

    @Test
    void 프론트_출처의_CORS_프리플라이트가_허용된다() throws Exception {
        mockMvc.perform(options("/api/v1/me")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void 없는_경로는_404로_내려간다() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    void 로그아웃하면_200이_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "20005", "로그아웃테스트");

        mockMvc.perform(post("/api/v1/logout")
                        .with(loginAs("20005")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void 회원탈퇴하면_회원과_응원기록이_함께_삭제된다() throws Exception {
        Member member = memberService.findOrCreate(Provider.KAKAO, "20006", "탈퇴테스트");
        Long itemId = cheeringItemRepository.findByActiveTrueOrderBySortOrder().get(0).getId();
        mockMvc.perform(post("/api/v1/cheerings/" + itemId)
                        .with(loginAs("20006")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/me")
                        .with(loginAs("20006")).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(memberRepository.findByProviderAndProviderId(Provider.KAKAO, "20006")).isEmpty();
        assertThat(cheeringRepository.countByMemberId(member.getId())).isZero();
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
