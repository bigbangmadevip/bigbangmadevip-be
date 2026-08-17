package com.thevip.member.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequestCreateRequest(@NotBlank String requestedRole) {
}
