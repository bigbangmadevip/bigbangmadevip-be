package com.thevip.music.repository;

import com.thevip.music.entity.MusicNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicNoticeRepository extends JpaRepository<MusicNotice, Long> {
}
