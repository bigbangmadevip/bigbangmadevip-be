package com.thevip.music.entity;

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
    private MusicCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    // 곡명/플랫폼/총공시간은 입력값이 없으면 화면에서 그 행 자체를 노출하지 않는 "선택 항목"이라 nullable.
    @Column(length = 100)
    private String songName;

    // 관련 플랫폼(Platform) id 참조 목록. FK 없이 ID 참조 방식. 예: 멜론+벅스 동시 총공.
    @ElementCollection
    @CollectionTable(name = "music_detail_platform", joinColumns = @JoinColumn(name = "music_detail_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "platform_id")
    private List<Long> platformIds = new ArrayList<>();

    // 총공 시작 시각. 날짜만 본다 — 시작일이 오늘이면 몇 시로 등록했든 오늘 0시부터 바로 노출된다
    // (VoteDetail의 eventStartAt과 동일한 규칙). 없으면 시작 제약 없음으로 본다.
    private LocalDateTime eventStartAt;

    // 총공 종료 시각. 이 시각이 지나면 노출 대상에서 자동으로 빠진다.
    private LocalDateTime eventEndAt;

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

    // 음원 메뉴 상단 고정 겸 홈 화면 긴급 배너 후보. 메뉴(음원)당 최대 하나만 켜져 있어야 하며
    // 이 불변식은 어드민 서비스가 강제한다 (DB 제약으로는 표현 불가). 홈 배너는 음원/투표 후보 중
    // 날짜(eventStartAt/eventEndAt)가 더 임박한 쪽을 HomeUrgentService가 골라서 보여준다.
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

    // 푸시 알림 발송 여부. 실제 발송(FCM 연동 등)은 아직 없고, 설정값만 저장해둔다.
    @Column(nullable = false)
    private boolean pushEnabled;

    // null이면 "게시 즉시" 발송, 값이 있으면 그 시각에 발송(예정) — scheduledAt과 같은 방식.
    private LocalDateTime pushSendAt;

    @Column(length = 26)
    private String pushTitle;

    @Column(length = 26)
    private String pushBody;

    // 실제 발송이 끝난 시각. 즉시발송/예약발송 모두 이 값으로 중복 발송을 막는다(null이면 미발송).
    private LocalDateTime pushSentAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MusicDetail of(MusicCategory category, String title, String songName,
            LocalDateTime eventStartAt, LocalDateTime eventEndAt) {
        MusicDetail detail = new MusicDetail();
        detail.category = category;
        detail.title = title;
        detail.songName = songName;
        detail.eventStartAt = eventStartAt;
        detail.eventEndAt = eventEndAt;
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

    public void addPlatformId(Long platformId) {
        this.platformIds.add(platformId);
    }

    public void replacePlatformIds(List<Long> platformIds) {
        this.platformIds.clear();
        this.platformIds.addAll(platformIds);
    }

    public void replaceChecklist(List<String> checklist) {
        this.checklist.clear();
        this.checklist.addAll(checklist);
    }

    public void replaceImageUrls(List<String> imageUrls) {
        this.imageUrls.clear();
        this.imageUrls.addAll(imageUrls);
    }

    public void replaceGuideIds(List<Long> guideIds) {
        this.guideIds.clear();
        this.guideIds.addAll(guideIds);
    }

    public void updateCore(MusicCategory category, String title, String songName, LocalDateTime eventStartAt,
            LocalDateTime eventEndAt) {
        this.category = category;
        this.title = title;
        this.songName = songName;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
    }

    public void updateActive(boolean active) {
        this.active = active;
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

    public void updatePushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public void updatePushSendAt(LocalDateTime pushSendAt) {
        this.pushSendAt = pushSendAt;
    }

    public void updatePushTitle(String pushTitle) {
        this.pushTitle = pushTitle;
    }

    public void updatePushBody(String pushBody) {
        this.pushBody = pushBody;
    }

    public void markPushSent() {
        this.pushSentAt = LocalDateTime.now();
    }

    public void resetPushSent() {
        this.pushSentAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
