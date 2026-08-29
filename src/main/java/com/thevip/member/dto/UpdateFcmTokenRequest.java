package com.thevip.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFcmTokenRequest(
        @NotBlank @Size(max = 255) String fcmToken) {
}
