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
public class VoteNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 투표 메뉴 상단 고정 여부. 메뉴당 (VoteDetail + VoteNotice 통틀어) 하나만 켜져 있어야 한다.
    @Column(nullable = false)
    private boolean menuUrgent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static VoteNotice of(String title, String content) {
        VoteNotice notice = new VoteNotice();
        notice.title = title;
        notice.content = content;
        notice.menuUrgent = false;
        notice.active = true;
        notice.createdAt = LocalDateTime.now();
        return notice;
    }

    public void updateMenuUrgent(boolean menuUrgent) {
        this.menuUrgent = menuUrgent;
    }
}
