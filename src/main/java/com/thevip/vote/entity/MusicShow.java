package com.thevip.vote.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 투표 중분류. 음악방송 프로그램(쇼! 음악중심, 뮤직뱅크 등)뿐 아니라 시상식/기념일 등
 * 다른 카테고리에서도 공통으로 쓰인다 (예: MAMA, MMA). VoteDetail.musicShowId로 참조된다.
 * platformIds는 실제 노출용 플랫폼 소스로는 쓰이지 않는다 — 플랫폼은 상세(VoteDetail)마다
 * 다를 수 있어 VoteDetail 자체의 platformIds를 쓴다 (VoteDetailPlatformResolver 참고).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // 이 방송에서 투표 가능한 플랫폼 후보 목록. FK 없이 ID 참조 방식.
    @ElementCollection
    @CollectionTable(name = "music_show_platform", joinColumns = @JoinColumn(name = "music_show_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "platform_id")
    private List<Long> platformIds = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(length = 20)
    private String channel;

    @Column(length = 50)
    private String broadcastTime;

    @Column(length = 500)
    private String iconUrl;

    // 투표 플랜 탭 상세 화면에 노출하는 방송 소개 문구.
    @Column(columnDefinition = "TEXT")
    private String description;

    // 관련 가이드(Guide) id 참조 목록. FK 없이 ID 참조 방식.
    @ElementCollection
    @CollectionTable(name = "music_show_guide", joinColumns = @JoinColumn(name = "music_show_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "guide_id")
    private List<Long> guideIds = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static MusicShow of(String name, int sortOrder) {
        MusicShow show = new MusicShow();
        show.name = name;
        show.sortOrder = sortOrder;
        show.active = true;
        show.createdAt = LocalDateTime.now();
        return show;
    }

    public void addPlatformId(Long platformId) {
        this.platformIds.add(platformId);
    }

    public void addGuideId(Long guideId) {
        this.guideIds.add(guideId);
    }

    public void replacePlatformIds(List<Long> platformIds) {
        this.platformIds.clear();
        this.platformIds.addAll(platformIds);
    }

    public void replaceGuideIds(List<Long> guideIds) {
        this.guideIds.clear();
        this.guideIds.addAll(guideIds);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void updateChannel(String channel) {
        this.channel = channel;
    }

    public void updateBroadcastTime(String broadcastTime) {
        this.broadcastTime = broadcastTime;
    }

    public void updateIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
