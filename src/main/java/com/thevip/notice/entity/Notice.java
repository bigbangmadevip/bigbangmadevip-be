package com.thevip.notice.entity;

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
 * 음원/투표 메뉴가 공용으로 쓰는 공지. menuType으로 어느 메뉴 공지인지만 구분한다 (Platform.type과 동일한 방식).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeMenuType menuType;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 최대 3개까지 (스펙 기준). 강제는 이 항목을 실제로 등록하는 어드민 API가 생길 때 서비스 레이어에서.
    @ElementCollection
    @CollectionTable(name = "notice_image", joinColumns = @JoinColumn(name = "notice_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", columnDefinition = "TEXT")
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    // 목록 상단 고정 여부. 여러 개 켜져 있으면 고정 항목끼리는 최신순으로 노출된다.
    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 화면에는 노출하지 않는 관리용 이력. 어드민 화면이 없어 수동 SQL로 갱신할 때 누가 고쳤는지 남겨둔다.
    private String updatedBy;

    public static Notice of(NoticeMenuType menuType, String title, String content) {
        Notice notice = new Notice();
        notice.menuType = menuType;
        notice.title = title;
        notice.content = content;
        notice.active = true;
        notice.createdAt = LocalDateTime.now();
        return notice;
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }

    public void updatePinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

    public void replaceImageUrls(List<String> imageUrls) {
        this.imageUrls.clear();
        this.imageUrls.addAll(imageUrls);
    }

    public void update(String title, String content, String updatedBy) {
        this.title = title;
        this.content = content;
        this.updatedBy = updatedBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
