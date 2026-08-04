package com.thevip.music.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 다운로드/스트리밍 등 카테고리 뱃지. CheeringItem과 같은 이유로 자유 문자열로 둔다.
    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false, length = 100)
    private String title;

    // 곡명/플랫폼/총공시간은 입력값이 없으면 화면에서 그 행 자체를 노출하지 않는 "선택 항목"이라 nullable.
    @Column(length = 100)
    private String songName;

    @Column(length = 50)
    private String platform;

    // 마감 시각이 아니라 "이 시각에 진행"하는 예정 시각. 음원 총공은 마감 개념이 없다.
    private LocalDateTime eventAt;

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

    // 음원 메뉴 상단 고정 여부. 메뉴당 (MusicDetail + MusicNotice 통틀어) 하나만 켜져 있어야 하며,
    // 이 불변식은 어드민 서비스가 강제한다 (DB 제약으로는 표현 불가, 두 테이블에 걸쳐 있어서).
    @Column(nullable = false)
    private boolean menuUrgent;

    // 홈 화면 긴급 배너 노출 여부. MusicDetail + VoteDetail 통틀어 하나만 켜져 있어야 한다.
    @Column(nullable = false)
    private boolean homeUrgent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static MusicDetail of(String category, String title, String songName, String platform,
            LocalDateTime eventAt) {
        MusicDetail detail = new MusicDetail();
        detail.category = category;
        detail.title = title;
        detail.songName = songName;
        detail.platform = platform;
        detail.eventAt = eventAt;
        detail.menuUrgent = false;
        detail.homeUrgent = false;
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

    public void updateMenuUrgent(boolean menuUrgent) {
        this.menuUrgent = menuUrgent;
    }

    public void updateHomeUrgent(boolean homeUrgent) {
        this.homeUrgent = homeUrgent;
    }
}
