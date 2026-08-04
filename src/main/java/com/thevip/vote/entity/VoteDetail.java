package com.thevip.vote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String platform;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    // 이 투표의 종료 시각. 화면에서 "마감까지 N분 남음" 계산에 쓰인다.
    private LocalDateTime eventEndAt;

    // 투표 메뉴 상단 고정 여부. 메뉴당 (VoteDetail + VoteNotice 통틀어) 하나만 켜져 있어야 하며,
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

    public static VoteDetail of(String title, String content, String platform, String imageUrl,
            LocalDateTime eventEndAt) {
        VoteDetail detail = new VoteDetail();
        detail.title = title;
        detail.content = content;
        detail.platform = platform;
        detail.imageUrl = imageUrl;
        detail.eventEndAt = eventEndAt;
        detail.menuUrgent = false;
        detail.homeUrgent = false;
        detail.active = true;
        detail.createdAt = LocalDateTime.now();
        return detail;
    }

    public void updateMenuUrgent(boolean menuUrgent) {
        this.menuUrgent = menuUrgent;
    }

    public void updateHomeUrgent(boolean homeUrgent) {
        this.homeUrgent = homeUrgent;
    }
}
