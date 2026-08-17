package com.thevip.member.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.member.dto.RoleRequestListItemResponse;
import com.thevip.member.dto.RoleRequestResponse;
import com.thevip.member.entity.Member;
import com.thevip.member.entity.MemberRoleRequest;
import com.thevip.member.entity.RequestStatus;
import com.thevip.member.entity.Role;
import com.thevip.member.repository.MemberRepository;
import com.thevip.member.repository.MemberRoleRequestRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberRoleRequestService {

    private final MemberRoleRequestRepository memberRoleRequestRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public RoleRequestResponse submit(Long memberId, String requestedRole) {
        Role role = parseRequestableRole(requestedRole);
        if (memberRoleRequestRepository.existsByMemberIdAndStatus(memberId, RequestStatus.PENDING)) {
            throw new BusinessException(ErrorCode.ROLE_REQUEST_ALREADY_PENDING);
        }

        MemberRoleRequest request = memberRoleRequestRepository.save(MemberRoleRequest.of(memberId, role));
        return RoleRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public RoleRequestResponse getMine(Long memberId) {
        return memberRoleRequestRepository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
                .map(RoleRequestResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<RoleRequestListItemResponse> listPending() {
        List<MemberRoleRequest> requests = memberRoleRequestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING);
        List<Long> memberIds = requests.stream().map(MemberRoleRequest::getMemberId).toList();
        Map<Long, String> nicknamesById = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        return requests.stream()
                .map(request -> RoleRequestListItemResponse.from(request, nicknamesById.get(request.getMemberId())))
                .toList();
    }

    @Transactional
    public void approve(Long requestId, Long resolverId) {
        MemberRoleRequest request = getPendingRequest(requestId);
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        member.updateRole(request.getRequestedRole());
        request.approve(resolverId);
    }

    @Transactional
    public void reject(Long requestId, Long resolverId) {
        MemberRoleRequest request = getPendingRequest(requestId);
        request.reject(resolverId);
    }

    private MemberRoleRequest getPendingRequest(Long requestId) {
        MemberRoleRequest request = memberRoleRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 권한 신청입니다."));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.ROLE_REQUEST_ALREADY_RESOLVED);
        }
        return request;
    }

    private Role parseRequestableRole(String requestedRole) {
        Role role;
        try {
            role = Role.valueOf(requestedRole);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 권한입니다.");
        }
        if (role != Role.MUSIC_ADMIN && role != Role.VOTE_ADMIN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "신청할 수 없는 권한입니다.");
        }
        return role;
    }
}
