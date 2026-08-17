package com.thevip.vote.repository;

import com.thevip.vote.entity.MusicShowVoteRound;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicShowVoteRoundRepository extends JpaRepository<MusicShowVoteRound, Long> {

    List<MusicShowVoteRound> findByMusicShowIdAndActiveTrueOrderBySortOrder(Long musicShowId);

    // 어드민 조회용 - 비활성 라운드도 함께 보여준다.
    List<MusicShowVoteRound> findByMusicShowIdOrderBySortOrder(Long musicShowId);

    // 라운드 수정 시 URL의 showId와 실제 소속 MusicShow가 일치하는지 확인하는 용도.
    Optional<MusicShowVoteRound> findByIdAndMusicShowId(Long id, Long musicShowId);
}
