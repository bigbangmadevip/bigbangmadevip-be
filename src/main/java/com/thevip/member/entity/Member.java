package com.thevip.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_member_provider_provider_id", columnNames = {"provider", "providerId"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(nullable = false, length = 100)
    private String providerId;

    // OAuth 제공자가 보내주는 이름. 재로그인할 때마다 최신값으로 동기화되며, 사용자가 직접 바꿀 수 없다.
    @Column(nullable = false, length = 50)
    private String name;

    // 앱 안에서 쓰는 표시 이름. 가입 시 name으로 초기화되고, 이후 사용자가 직접 수정할 수 있다.
    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // 로그인 직후 약관동의 화면을 띄울지 프론트가 판단하는 기준. 항목이 하나(전체 동의)라 단일 플래그로 둔다.
    @Column(nullable = false)
    private boolean termsAgreed;

    private LocalDateTime termsAgreedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Member of(Provider provider, String providerId, String name) {
        Member member = new Member();
        member.provider = provider;
        member.providerId = providerId;
        member.name = name;
        member.nickname = name;
        member.role = Role.USER;
        member.termsAgreed = false;
        member.createdAt = LocalDateTime.now();
        return member;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void agreeToTerms() {
        this.termsAgreed = true;
        this.termsAgreedAt = LocalDateTime.now();
    }
}
