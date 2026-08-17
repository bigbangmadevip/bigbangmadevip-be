package com.thevip.vote.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VoteRoundRow {

    @Column(length = 30)
    private String label;

    // "value"는 H2 예약어라 컬럼명으로 그대로 쓰면 테이블 생성이 조용히 실패한다.
    @Column(name = "row_value", length = 200)
    private String value;
}
