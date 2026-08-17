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

    public static Platform of(String name, PlatformType type, PlatformRegion region, String iconUrl) {
        Platform platform = new Platform();
        platform.name = name;
        platform.type = type;
        platform.region = region;
        platform.iconUrl = iconUrl;
        platform.active = true;
        return platform;
    }

    public void update(String name, PlatformType type, PlatformRegion region, String iconUrl) {
        this.name = name;
        this.type = type;
        this.region = region;
        this.iconUrl = iconUrl;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }
}
