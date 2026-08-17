package com.thevip.music.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 원클릭 스트리밍 화면에서 플랫폼(Platform) -> 운영체제 -> 링크 목록으로 드릴다운할 때 쓰는 개별 링크.
 * 같은 platformId + os 조합에 링크가 여러 개 있을 수 있고, sortOrder로 정렬한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicStreamingLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long platformId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperatingSystem os;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MusicStreamingLink of(Long platformId, OperatingSystem os, String label, String url,
            int sortOrder) {
        MusicStreamingLink link = new MusicStreamingLink();
        link.platformId = platformId;
        link.os = os;
        link.label = label;
        link.url = url;
        link.sortOrder = sortOrder;
        link.active = true;
        link.createdAt = LocalDateTime.now();
        return link;
    }

    public void update(Long platformId, OperatingSystem os, String label, String url, int sortOrder) {
        this.platformId = platformId;
        this.os = os;
        this.label = label;
        this.url = url;
        this.sortOrder = sortOrder;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
