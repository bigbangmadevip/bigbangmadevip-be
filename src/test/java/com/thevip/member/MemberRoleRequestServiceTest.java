package com.thevip.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.global.exception.BusinessException;
import com.thevip.member.entity.Member;
import com.thevip.member.entity.MemberRoleRequest;
import com.thevip.member.entity.Provider;
import com.thevip.member.entity.RequestStatus;
import com.thevip.member.entity.Role;
import com.thevip.member.repository.MemberRepository;
import com.thevip.member.repository.MemberRoleRequestRepository;
import com.thevip.member.service.MemberRoleRequestService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MemberRoleRequestServiceTest {

    @Test
    void 신청하면_대기_상태로_저장된다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        when(requestRepository.existsByMemberIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(false);
        when(requestRepository.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);
        var result = service.submit(1L, "MUSIC_ADMIN");

        assertThat(result.requestedRole()).isEqualTo("MUSIC_ADMIN");
        assertThat(result.status()).isEqualTo("PENDING");
    }

    @Test
    void 이미_대기중이면_예외가_발생한다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);
        when(requestRepository.existsByMemberIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(true);

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);

        assertThatThrownBy(() -> service.submit(1L, "MUSIC_ADMIN")).isInstanceOf(BusinessException.class);
    }

    @Test
    void MASTER는_신청할_수_없다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);

        assertThatThrownBy(() -> service.submit(1L, "MASTER")).isInstanceOf(BusinessException.class);
    }

    @Test
    void 승인하면_회원_역할이_바뀐다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.MUSIC_ADMIN);
        Member member = Member.of(Provider.KAKAO, "1", "테스트");
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);
        service.approve(100L, 9L, Role.MASTER);

        assertThat(member.getRole()).isEqualTo(Role.MUSIC_ADMIN);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void 같은_도메인_관리자도_승인할_수_있다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.VOTE_ADMIN);
        Member member = Member.of(Provider.KAKAO, "1", "테스트");
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);
        service.approve(100L, 9L, Role.VOTE_ADMIN);

        assertThat(member.getRole()).isEqualTo(Role.VOTE_ADMIN);
    }

    @Test
    void 다른_도메인_관리자가_승인하면_예외가_발생한다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.VOTE_ADMIN);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);

        assertThatThrownBy(() -> service.approve(100L, 9L, Role.MUSIC_ADMIN)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 이미_처리된_신청을_승인하면_예외가_발생한다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.MUSIC_ADMIN);
        request.approve(9L);
        when(requestRepository.findById(100L)).thenReturn(Optional.of(request));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);

        assertThatThrownBy(() -> service.approve(100L, 9L, Role.MASTER)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 마스터는_대기중인_신청_전부를_닉네임과_함께_반환받는다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.VOTE_ADMIN);
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(member.getNickname()).thenReturn("닉네임테스트");
        when(requestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING)).thenReturn(List.of(request));
        when(memberRepository.findAllById(List.of(1L))).thenReturn(List.of(member));

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);
        var result = service.listPending(Role.MASTER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).memberNickname()).isEqualTo("닉네임테스트");
    }

    @Test
    void 도메인_관리자는_자기_도메인_신청만_반환받는다() {
        MemberRoleRequestRepository requestRepository = mock(MemberRoleRequestRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        MemberRoleRequest request = MemberRoleRequest.of(1L, Role.VOTE_ADMIN);
        when(requestRepository.findByStatusAndRequestedRoleOrderByCreatedAtAsc(RequestStatus.PENDING, Role.VOTE_ADMIN))
                .thenReturn(List.of(request));
        when(memberRepository.findAllById(List.of(1L))).thenReturn(List.of());

        MemberRoleRequestService service = new MemberRoleRequestService(requestRepository, memberRepository);
        var result = service.listPending(Role.VOTE_ADMIN);

        assertThat(result).hasSize(1);
    }
}
