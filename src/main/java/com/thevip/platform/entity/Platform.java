package com.thevip.platform.entity;

import jakarta.persistence.*;
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

    // 어드민 API가 platformId(auto-increment) 대신 참조용으로 쓰는 안정적인 식별자(예: "melon").
    // id는 등록 순서에 따라 환경마다 달라질 수 있어 프론트가 하드코딩하기 위험하지만, code는 등록
    // 시점에 직접 정하는 값이라 환경/순서와 무관하게 항상 같다.
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    // 음원 상세/원클릭 스트리밍용 플랫폼인지, 투표 상세용 플랫폼인지 구분. 한 플랫폼이 둘 다 겸하는
    // 경우는 아직 없어서 단일 값으로 둔다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformRegion region;

    @Column(columnDefinition = "TEXT")
    private String iconUrl;

    @Column(nullable = false)
    private boolean active;

    public static Platform of(String name, String code, PlatformType type, PlatformRegion region, String iconUrl) {
        Platform platform = new Platform();
        platform.name = name;
        platform.code = code;
        platform.type = type;
        platform.region = region;
        platform.iconUrl = iconUrl;
        platform.active = true;
        return platform;
    }

    public void update(String name, String code, PlatformType type, PlatformRegion region, String iconUrl) {
        this.name = name;
        this.code = code;
        this.type = type;
        this.region = region;
        this.iconUrl = iconUrl;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }
}
