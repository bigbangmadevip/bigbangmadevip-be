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
 * 음악방송 프로그램(쇼! 음악중심, 뮤직뱅크 등). 투표 총공 등록 화면에서 "총공 구분"으로 이 방송을
 * 고르면, 여기 담긴 platformIds가 "총공 플랫폼" 선택지로 노출된다 (예: 쇼음악중심 -> 뮤빗/뮤니버스).
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
}
