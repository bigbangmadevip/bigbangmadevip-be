package com.thevip.member.repository;

import com.thevip.member.entity.MemberRoleRequest;
import com.thevip.member.entity.RequestStatus;
import com.thevip.member.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoleRequestRepository extends JpaRepository<MemberRoleRequest, Long> {

    boolean existsByMemberIdAndStatus(Long memberId, RequestStatus status);

    Optional<MemberRoleRequest> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<MemberRoleRequest> findByStatusOrderByCreatedAtAsc(RequestStatus status);

    List<MemberRoleRequest> findByStatusAndRequestedRoleOrderByCreatedAtAsc(RequestStatus status, Role requestedRole);
}
