package com.thevip.platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음원/투표 상세와 가이드가 공통으로 참조하는 플랫폼(멜론, 지니, 하이어 등) 마스터 데이터.
 * ID 참조 방식(FK 없음)으로 MusicDetail/VoteDetail/Guide에서 platformId로 가리킨다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformRegion region;

    @Column(columnDefinition = "TEXT")
    private String iconUrl;

    public static Platform of(String name, PlatformRegion region, String iconUrl) {
        Platform platform = new Platform();
        platform.name = name;
        platform.region = region;
        platform.iconUrl = iconUrl;
        return platform;
    }
}
