package com.thevip.member.dto;

import com.thevip.member.entity.Member;

public record MemberResponse(Long id, String name, String nickname, String provider, String role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getNickname(),
                member.getProvider().name(),
                member.getRole().name());
    }
}
