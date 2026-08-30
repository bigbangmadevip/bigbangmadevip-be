package com.thevip.member.service;

import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.member.entity.Member;
import com.thevip.member.entity.Provider;
import com.thevip.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CheeringRepository cheeringRepository;

    @Transactional(readOnly = true)
    public Member getByProviderId(Provider provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Member getCurrentMember(OAuth2AuthenticationToken authentication) {
        Provider provider = Provider.valueOf(authentication.getAuthorizedClientRegistrationId().toUpperCase());
        return getByProviderId(provider, authentication.getName());
    }

    @Transactional
    public Member findOrCreate(Provider provider, String providerId, String name) {
        return findOrCreate(provider, providerId, name, null);
    }

    @Transactional
    public Member findOrCreate(Provider provider, String providerId, String name, String email) {
        return memberRepository.findByProviderAndProviderId(provider, providerId)
                .map(member -> {
                    member.updateName(name);
                    member.updateEmail(email);
                    return member;
                })
                .orElseGet(() -> {
                    Member member = Member.of(provider, providerId, name);
                    member.updateEmail(email);
                    return memberRepository.save(member);
                });
    }

    @Transactional
    public Member updateNickname(Long memberId, String nickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        member.updateNickname(nickname);
        return member;
    }

    @Transactional
    public Member agreeToTerms(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        member.agreeToTerms();
        return member;
    }

    @Transactional
    public void updateFcmToken(Long memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        member.updateFcmToken(fcmToken);
    }

    @Transactional
    public Member updatePushSettings(
            Long memberId, boolean urgentPushEnabled, boolean musicPushEnabled, boolean votePushEnabled) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        member.updatePushSettings(urgentPushEnabled, musicPushEnabled, votePushEnabled);
        return member;
    }

    @Transactional
    public void withdraw(Long memberId) {
        cheeringRepository.deleteByMemberId(memberId);
        memberRepository.deleteById(memberId);
    }
}
