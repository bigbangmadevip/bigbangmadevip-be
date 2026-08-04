package com.thevip.link.repository;

import com.thevip.link.entity.ShortLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortKey(String shortKey);

    boolean existsByShortKey(String shortKey);
}
