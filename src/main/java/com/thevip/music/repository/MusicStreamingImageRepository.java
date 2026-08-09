package com.thevip.music.repository;

import com.thevip.music.entity.MusicStreamingImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicStreamingImageRepository extends JpaRepository<MusicStreamingImage, Long> {

    List<MusicStreamingImage> findByActiveTrueOrderBySortOrderAsc();
}
