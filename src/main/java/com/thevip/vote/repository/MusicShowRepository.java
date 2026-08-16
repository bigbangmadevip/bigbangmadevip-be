package com.thevip.vote.repository;

import com.thevip.vote.entity.MusicShow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicShowRepository extends JpaRepository<MusicShow, Long> {

    List<MusicShow> findByActiveTrueOrderBySortOrder();
}
