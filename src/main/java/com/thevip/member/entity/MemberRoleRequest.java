package com.thevip.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 권한(MUSIC_ADMIN/VOTE_ADMIN) 신청 1건. 신청자는 자기 신청만 볼 수 있고, 승인/반려는
 * MASTER만 할 수 있다(부트스트랩 문제 없이 항상 승인 권한자가 존재하도록). MASTER 권한 자체는
 * 이 신청 흐름 대상이 아니며 별도로(DB 등) 부여한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청한 Member. FK 없이 ID 참조 방식.
    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    // 승인/반려한 Member(MASTER). FK 없이 ID 참조 방식.
    private Long resolvedBy;

    public static MemberRoleRequest of(Long memberId, Role requestedRole) {
        MemberRoleRequest request = new MemberRoleRequest();
        request.memberId = memberId;
        request.requestedRole = requestedRole;
        request.status = RequestStatus.PENDING;
        request.createdAt = LocalDateTime.now();
        return request;
    }

    public void approve(Long resolvedBy) {
        this.status = RequestStatus.APPROVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }

    public void reject(Long resolvedBy) {
        this.status = RequestStatus.REJECTED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }
}
