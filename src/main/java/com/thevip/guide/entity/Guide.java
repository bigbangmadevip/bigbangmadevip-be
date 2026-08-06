package com.thevip.guide.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 음원/투표 상세에서 공통으로 참조하는 가이드 게시물. MusicDetail/VoteDetail은 이 id 목록(guideIds)만 갖는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuideType guideType;

    private Long platformId;

    @Column(nullable = false, length = 100)
    private String title;

    @ElementCollection
    @CollectionTable(name = "guide_image", joinColumns = @JoinColumn(name = "guide_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Guide of(GuideType guideType, Long platformId, String title, int sortOrder) {
        Guide guide = new Guide();
        guide.guideType = guideType;
        guide.platformId = platformId;
        guide.title = title;
        guide.sortOrder = sortOrder;
        guide.active = true;
        guide.createdAt = LocalDateTime.now();
        return guide;
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
