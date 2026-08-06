package com.thevip.vote.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoteCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String rewardDescription;

    private Long platformId;

    @Column(columnDefinition = "TEXT")
    private String platformUrl;

    private LocalDateTime eventStartAt;

    // 이 투표의 종료 시각. 화면에서 "마감까지 N분 남음" 계산에 쓰인다.
    private LocalDateTime eventEndAt;

    @ElementCollection
    @CollectionTable(name = "vote_detail_checklist", joinColumns = @JoinColumn(name = "vote_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "content", columnDefinition = "TEXT")
    private List<String> checklist = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "vote_detail_image", joinColumns = @JoinColumn(name = "vote_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    // 관련 가이드(Guide) id 참조 목록. FK 없이 ID 참조 방식.
    @ElementCollection
    @CollectionTable(name = "vote_detail_guide", joinColumns = @JoinColumn(name = "vote_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "guide_id")
    private List<Long> guideIds = new ArrayList<>();

    @Column(length = 30)
    private String ctaButtonLabel;

    // "오늘의 응원" 노출용으로 연결한 CheeringItem. null이면 오늘의 응원에 안 뜬다.
    private Long cheeringItemId;

    // 투표 메뉴 상단 고정 겸 홈 화면 긴급 배너 후보. 메뉴(투표)당 최대 하나만 켜져 있어야 하며
    // 이 불변식은 어드민 서비스가 강제한다 (DB 제약으로는 표현 불가). 홈 배너는 음원/투표 후보 중
    // 날짜(eventAt/eventEndAt)가 더 임박한 쪽을 HomeUrgentService가 골라서 보여준다.
    @Column(nullable = false)
    private boolean menuUrgent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static VoteDetail of(VoteCategory category, String title, String rewardDescription, Long platformId,
            LocalDateTime eventStartAt, LocalDateTime eventEndAt, int sortOrder) {
        VoteDetail detail = new VoteDetail();
        detail.category = category;
        detail.title = title;
        detail.rewardDescription = rewardDescription;
        detail.platformId = platformId;
        detail.eventStartAt = eventStartAt;
        detail.eventEndAt = eventEndAt;
        detail.sortOrder = sortOrder;
        detail.menuUrgent = false;
        detail.active = true;
        detail.createdAt = LocalDateTime.now();
        return detail;
    }

    public void addChecklistItem(String item) {
        this.checklist.add(item);
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }

    public void addGuideId(Long guideId) {
        this.guideIds.add(guideId);
    }

    public void updatePlatformUrl(String platformUrl) {
        this.platformUrl = platformUrl;
    }

    public void updateCtaButtonLabel(String ctaButtonLabel) {
        this.ctaButtonLabel = ctaButtonLabel;
    }

    public void updateCheeringItemId(Long cheeringItemId) {
        this.cheeringItemId = cheeringItemId;
    }

    public void updateMenuUrgent(boolean menuUrgent) {
        this.menuUrgent = menuUrgent;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
