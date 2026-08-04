package com.thevip.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.thevip.member.entity.Member;
import com.thevip.member.entity.Provider;
import com.thevip.member.entity.Role;
import com.thevip.member.service.MemberService;
import com.thevip.member.service.OAuthProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void 처음_로그인하면_USER_역할로_가입되고_닉네임은_이름으로_초기화된다() {
        Member member = memberService.findOrCreate(Provider.KAKAO, "10001", "종식");

        assertThat(member.getId()).isNotNull();
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getName()).isEqualTo("종식");
        assertThat(member.getNickname()).isEqualTo("종식");
    }

    @Test
    void 재로그인하면_OAuth_이름은_갱신되지만_직접_정한_닉네임은_유지된다() {
        Member first = memberService.findOrCreate(Provider.KAKAO, "10002", "카카오닉네임1");
        memberService.updateNickname(first.getId(), "내가정한별명");

        Member second = memberService.findOrCreate(Provider.KAKAO, "10002", "카카오닉네임2");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getName()).isEqualTo("카카오닉네임2");
        assertThat(second.getNickname()).isEqualTo("내가정한별명");
    }

    @Test
    void 카카오_응답에서_프로필을_추출한다() {
        Map<String, Object> attributes = Map.of(
                "id", 12345678L,
                "properties", Map.of("nickname", "홍길동"));

        OAuthProfile profile = OAuthProfile.from("kakao", attributes);

        assertThat(profile.provider()).isEqualTo(Provider.KAKAO);
        assertThat(profile.providerId()).isEqualTo("12345678");
        assertThat(profile.name()).isEqualTo("홍길동");
    }

    @Test
    void 닉네임_동의를_안하면_기본_이름이_들어간다() {
        OAuthProfile profile = OAuthProfile.from("kakao", Map.of("id", 999L));

        assertThat(profile.name()).isEqualTo("VIP");
    }
}
