package com.thevip.cheering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cheering_member_item_date",
                columnNames = {"memberId", "itemId", "cheeringDate"}),
        indexes = @Index(name = "idx_cheering_date_member", columnList = "cheeringDate, memberId"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cheering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, updatable = false)
    private LocalDate cheeringDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Cheering of(Long memberId, Long itemId) {
        Cheering cheering = new Cheering();
        cheering.memberId = memberId;
        cheering.itemId = itemId;
        cheering.cheeringDate = LocalDate.now();
        cheering.createdAt = LocalDateTime.now();
        return cheering;
    }
}
