package com.thevip.music.repository;

import com.thevip.music.entity.MusicDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MusicDetailRepository extends JpaRepository<MusicDetail, Long> {

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    @Query("SELECT m FROM MusicDetail m WHERE m.menuUrgent = true AND m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now)")
    List<MusicDetail> findVisibleMenuUrgent(@Param("now") LocalDateTime now);

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // eventAt이 오늘 날짜인 것만 "오늘의 총공 일정"에 노출한다.
    @Query("SELECT m FROM MusicDetail m WHERE m.todayExposed = true AND m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND m.eventAt >= :startOfToday AND m.eventAt < :startOfTomorrow "
            + "ORDER BY m.eventAt ASC")
    List<MusicDetail> findTodayExposed(@Param("now") LocalDateTime now,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);
}
