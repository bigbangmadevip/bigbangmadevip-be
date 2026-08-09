package com.thevip.music.repository;

import com.thevip.music.entity.MusicStreamingLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicStreamingLinkRepository extends JpaRepository<MusicStreamingLink, Long> {

    List<MusicStreamingLink> findByActiveTrueOrderByPlatformIdAscSortOrderAsc();
}
