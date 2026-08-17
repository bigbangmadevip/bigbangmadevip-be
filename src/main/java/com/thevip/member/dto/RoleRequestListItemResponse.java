package com.thevip.member.dto;

import com.thevip.member.entity.MemberRoleRequest;
import java.time.LocalDateTime;

public record RoleRequestListItemResponse(Long id, Long memberId, String memberNickname, String requestedRole,
        LocalDateTime createdAt) {

    public static RoleRequestListItemResponse from(MemberRoleRequest request, String memberNickname) {
        return new RoleRequestListItemResponse(
                request.getId(),
                request.getMemberId(),
                memberNickname,
                request.getRequestedRole().name(),
                request.getCreatedAt());
    }
}
