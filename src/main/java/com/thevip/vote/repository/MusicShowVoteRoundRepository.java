package com.thevip.vote.repository;

import com.thevip.vote.entity.MusicShowVoteRound;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicShowVoteRoundRepository extends JpaRepository<MusicShowVoteRound, Long> {

    List<MusicShowVoteRound> findByMusicShowIdAndActiveTrueOrderBySortOrder(Long musicShowId);
}
