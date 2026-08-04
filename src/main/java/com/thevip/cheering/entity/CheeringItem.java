package com.thevip.cheering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheeringItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheeringCategory category;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 100)
    private String subtitle;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    public static CheeringItem of(CheeringCategory category, String title, String subtitle, int sortOrder) {
        CheeringItem item = new CheeringItem();
        item.category = category;
        item.title = title;
        item.subtitle = subtitle;
        item.sortOrder = sortOrder;
        item.active = true;
        return item;
    }
}
