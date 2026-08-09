package com.thevip.music.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 원클릭 스트리밍 화면 하단 "스트리밍 리스트" 섹션에 노출하는 추천 플레이리스트 이미지.
 * 특정 총공(MusicDetail)과 무관하게 탭 전체에 걸린 공용 콘텐츠다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicStreamingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MusicStreamingImage of(String imageUrl, int sortOrder) {
        MusicStreamingImage image = new MusicStreamingImage();
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        image.active = true;
        image.createdAt = LocalDateTime.now();
        return image;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
