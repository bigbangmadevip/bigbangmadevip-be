package com.thevip.vote.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
 * MusicShow의 주간 투표 라운드 하나(예: 사전 투표 1, 생방송 투표, 문자 투표). 방송마다 라운드
 * 개수와 종류가 제각각이라 고정된 필드가 아니라 이 리스트로 표현한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicShowVoteRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 MusicShow. FK 없이 ID 참조 방식.
    @Column(nullable = false)
    private Long musicShowId;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(length = 50)
    private String time;

    // 라운드 종류를 나타내는 자유 문자열(예: advance/live/text/mpick). 방송마다 새로운 종류가
    // 계속 늘어날 수 있어 고정 enum 대신 문자열로 열어두고, 프론트가 이 값으로 색상을 매핑한다.
    @Column(length = 20)
    private String tone;

    @ElementCollection
    @CollectionTable(name = "music_show_vote_round_row", joinColumns = @JoinColumn(name = "vote_round_id"))
    @OrderColumn(name = "sort_order")
    private List<VoteRoundRow> rows = new ArrayList<>();

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MusicShowVoteRound of(Long musicShowId, String label, String time, String tone, int sortOrder) {
        MusicShowVoteRound round = new MusicShowVoteRound();
        round.musicShowId = musicShowId;
        round.label = label;
        round.time = time;
        round.tone = tone;
        round.sortOrder = sortOrder;
        round.active = true;
        round.createdAt = LocalDateTime.now();
        return round;
    }

    public void addRow(String label, String value) {
        this.rows.add(new VoteRoundRow(label, value));
    }

    public void replaceRows(List<VoteRoundRow> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
    }

    public void updateCore(String label, String time, String tone, int sortOrder) {
        this.label = label;
        this.time = time;
        this.tone = tone;
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
