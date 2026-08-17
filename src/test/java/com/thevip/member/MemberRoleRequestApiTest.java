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
        givenRole("30007", "마스터테스트", Role.MASTER);

        String requestId = mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30006", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"MUSIC_ADMIN\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(requestId).read("$.data.id", Long.class);

        List<Map<String, Object>> pending = pendingList(loginAs("30007", "MASTER"));
        assertThat(pending).anyMatch(item -> "승인대상테스트".equals(item.get("memberNickname")));

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

    @Test
    void 같은_도메인_관리자가_승인하면_역할이_바뀐다() throws Exception {
        Member applicant = memberService.findOrCreate(Provider.KAKAO, "30008", "투표승인대상테스트");
        givenRole("30009", "투표관리자테스트", Role.VOTE_ADMIN);

        String response = mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30008", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"VOTE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.data.id", Long.class);

        List<Map<String, Object>> pending = pendingList(loginAs("30009", "VOTE_ADMIN"));
        assertThat(pending).anyMatch(item -> "투표승인대상테스트".equals(item.get("memberNickname")));

        mockMvc.perform(post("/api/v1/admin/role-requests/" + id + "/approve")
                        .with(loginAs("30009", "VOTE_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());

        Member updated = memberRepository.findById(applicant.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.VOTE_ADMIN);
    }

    @Test
    void 다른_도메인_관리자가_승인하면_403이다() throws Exception {
        memberService.findOrCreate(Provider.KAKAO, "30010", "다른도메인신청테스트");
        givenRole("30011", "음원관리자테스트", Role.MUSIC_ADMIN);

        String response = mockMvc.perform(post("/api/v1/admin/role-requests")
                        .with(loginAs("30010", "USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestedRole\":\"VOTE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.data.id", Long.class);

        List<Map<String, Object>> pending = pendingList(loginAs("30011", "MUSIC_ADMIN"));
        assertThat(pending).noneMatch(item -> "다른도메인신청테스트".equals(item.get("memberNickname")));

        mockMvc.perform(post("/api/v1/admin/role-requests/" + id + "/approve")
                        .with(loginAs("30011", "MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("C003"));
    }

    // loginAs가 심어주는 권한(SecurityConfig 통과용)과 별개로, 컨트롤러는 DB의 Member.role을
    // 직접 조회해서 승인 범위를 판단하므로 실제 회원 role도 맞춰줘야 한다.
    private void givenRole(String kakaoId, String nickname, Role role) {
        Member member = memberService.findOrCreate(Provider.KAKAO, kakaoId, nickname);
        member.updateRole(role);
        memberRepository.save(member);
    }

    // 이 컨텍스트는 테스트끼리 공유돼 이전 테스트가 만든 대기중 신청도 같이 섞여 나오므로,
    // 인덱스가 아니라 원하는 멤버가 포함돼 있는지로 검증한다.
    private List<Map<String, Object>> pendingList(RequestPostProcessor login) throws Exception {
        String response = mockMvc.perform(get("/api/v1/admin/role-requests").with(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.parse(response).read("$.data");
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
