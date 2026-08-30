package com.thevip.member.dto;

import com.thevip.member.entity.Member;

public record MemberResponse(Long id, String name, String nickname, String email, String provider, String role,
        boolean termsAgreed, Boolean urgentPushEnabled, Boolean musicPushEnabled, Boolean votePushEnabled) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getEmail(),
                member.getProvider().name(),
                member.getRole().name(),
                member.isTermsAgreed(),
                member.getUrgentPushEnabled(),
                member.getMusicPushEnabled(),
                member.getVotePushEnabled());
    }
}
