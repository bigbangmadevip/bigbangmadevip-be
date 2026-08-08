package com.thevip.music.entity;

import com.thevip.cheering.entity.CheeringCategory;
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
public class MusicDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheeringCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    // 곡명/플랫폼/총공시간은 입력값이 없으면 화면에서 그 행 자체를 노출하지 않는 "선택 항목"이라 nullable.
    @Column(length = 100)
    private String songName;

    private Long platformId;

    @Column(columnDefinition = "TEXT")
    private String platformUrl;

    // 마감 시각이 아니라 "이 시각에 진행"하는 예정 시각. 음원 총공은 마감 개념이 없다.
    private LocalDateTime eventAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "music_detail_checklist", joinColumns = @JoinColumn(name = "music_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "content", columnDefinition = "TEXT")
    private List<String> checklist = new ArrayList<>();

    // 최대 3개까지 (스펙 기준). 강제는 이 항목을 실제로 등록하는 어드민 API가 생길 때 서비스 레이어에서.
    @ElementCollection
    @CollectionTable(name = "music_detail_image", joinColumns = @JoinColumn(name = "music_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    // 관련 가이드(Guide) id 참조 목록. FK 없이 ID 참조 방식.
    @ElementCollection
    @CollectionTable(name = "music_detail_guide", joinColumns = @JoinColumn(name = "music_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "guide_id")
    private List<Long> guideIds = new ArrayList<>();

    // "오늘의 응원" 노출용으로 연결한 CheeringItem. null이면 오늘의 응원에 안 뜬다.
    private Long cheeringItemId;

    // 음원 메뉴 상단 고정 겸 홈 화면 긴급 배너 후보. 메뉴(음원)당 최대 하나만 켜져 있어야 하며
    // 이 불변식은 어드민 서비스가 강제한다 (DB 제약으로는 표현 불가). 홈 배너는 음원/투표 후보 중
    // 날짜(eventAt/eventEndAt)가 더 임박한 쪽을 HomeUrgentService가 골라서 보여준다.
    @Column(nullable = false)
    private boolean menuUrgent;

    // 긴급 배너에 노출할 때 쓰는 문구. title과 별개 (title은 관리용 제목, 이건 배너 노출용 짧은 문구).
    @Column(length = 26)
    private String urgentContent;

    @Column(nullable = false)
    private boolean active;

    // 지정하면 이 시각이 지나기 전까지는 active=true여도 노출 대상에서 제외한다 (예약 등록).
    // 배치 없이 조회 시점에 계산하는 방식(MusicDetailRepository 참고).
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MusicDetail of(CheeringCategory category, String title, String songName, Long platformId,
            LocalDateTime eventAt, int sortOrder) {
        MusicDetail detail = new MusicDetail();
        detail.category = category;
        detail.title = title;
        detail.songName = songName;
        detail.platformId = platformId;
        detail.eventAt = eventAt;
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

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePlatformUrl(String platformUrl) {
        this.platformUrl = platformUrl;
    }

    public void updateCheeringItemId(Long cheeringItemId) {
        this.cheeringItemId = cheeringItemId;
    }

    public void updateMenuUrgent(boolean menuUrgent) {
        this.menuUrgent = menuUrgent;
    }

    public void updateUrgentContent(String urgentContent) {
        this.urgentContent = urgentContent;
    }

    public void updateScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
