package com.thevip.music.repository;

import com.thevip.music.entity.MusicDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicDetailRepository extends JpaRepository<MusicDetail, Long> {

    List<MusicDetail> findByHomeUrgentTrueAndActiveTrue();
}
