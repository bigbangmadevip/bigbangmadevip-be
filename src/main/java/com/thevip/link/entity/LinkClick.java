package com.thevip.link.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_link_click_short_link_id", columnList = "shortLinkId"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shortLinkId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime clickedAt;

    public static LinkClick of(Long shortLinkId) {
        LinkClick click = new LinkClick();
        click.shortLinkId = shortLinkId;
        click.clickedAt = LocalDateTime.now();
        return click;
    }
}
