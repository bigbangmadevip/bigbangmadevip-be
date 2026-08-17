package com.thevip.member.dto;

import com.thevip.member.entity.MemberRoleRequest;
import java.time.LocalDateTime;

public record RoleRequestResponse(Long id, String requestedRole, String status, LocalDateTime createdAt,
        LocalDateTime resolvedAt) {

    public static RoleRequestResponse from(MemberRoleRequest request) {
        return new RoleRequestResponse(
                request.getId(),
                request.getRequestedRole().name(),
                request.getStatus().name(),
                request.getCreatedAt(),
                request.getResolvedAt());
    }
}
