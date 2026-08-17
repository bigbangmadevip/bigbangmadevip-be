package com.thevip.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.member.entity.Member;
import com.thevip.member.entity.Provider;
import com.thevip.member.entity.Role;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class MemberRoleRequestApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 권한을_신청하면_대기_상태로_생성된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30001", "신청테스트");

        mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30001", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"MUSIC_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestedRole").value("MUSIC_ADMIN"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void 이미_대기중인_신청이_있으면_409가_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30002", "중복신청테스트");
        mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30002", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"MUSIC_ADMIN\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30002", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"VOTE_ADMIN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("RR001"));
    }

    @Test
    void 신청할_수_없는_권한이면_400이_반환된다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30003", "잘못된권한테스트");

        mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30003", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"MASTER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    void 내_신청_상태를_조회한다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30004", "내신청조회테스트");
        mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30004", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"VOTE_ADMIN\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/role-requests/me").with(loginAs("30004", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestedRole").value("VOTE_ADMIN"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void 마스터가_아니면_대기_목록_조회와_승인이_403이다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30005", "권한없음테스트");

        mockMvc.perform(get("/api/v1/admin/role-requests").with(loginAs("30005", "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));

        mockMvc.perform(post("/api/v1/admin/role-requests/1/approve")
                        .with(loginAs("30005", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));
    }

    @Test
    void 마스터가_승인하면_회원_역할이_바뀌고_재승인은_409다() throws Exception {
        Member applicant = memberService.findOrCreate(Provider.KAKAO, "30006", "승인대상테스트");
        memberService.findOrCreate(Provider.KAKAO, "30007", "마스터테스트");

        String requestId = mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30006", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"MUSIC_ADMIN\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(requestId).read("$.data.id", Long.class);

        mockMvc.perform(get("/api/v1/admin/role-requests").with(loginAs("30007", "MASTER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].memberNickname").value("승인대상테스트"));

        mockMvc.perform(post("/api/v1/admin/role-requests/" + id + "/approve")
                        .with(loginAs("30007", "MASTER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        Member updated = memberRepository.findById(applicant.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.MUSIC_ADMIN);

        mockMvc.perform(post("/api/v1/admin/role-requests/" + id + "/approve")
                        .with(loginAs("30007", "MASTER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("RR002"));
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
