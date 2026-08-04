package com.thevip.link.entity;

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
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ShortLink of(String shortKey, String originalUrl, String title) {
        ShortLink link = new ShortLink();
        link.shortKey = shortKey;
        link.originalUrl = originalUrl;
        link.title = title;
        link.createdAt = LocalDateTime.now();
        return link;
    }
}
