package com.thevip.member.dto;

public record UpdatePushSettingsRequest(
        boolean urgentPushEnabled,
        boolean musicPushEnabled,
        boolean votePushEnabled) {
}
