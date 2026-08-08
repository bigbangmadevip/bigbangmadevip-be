package com.thevip.vote.repository;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteDetailRepository extends JpaRepository<VoteDetail, Long> {

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    @Query("SELECT v FROM VoteDetail v WHERE v.menuUrgent = true AND v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now)")
    List<VoteDetail> findVisibleMenuUrgent(@Param("now") LocalDateTime now);

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // eventEndAt이 오늘 날짜인 것만 "오늘의 총공 일정"에 노출한다.
    @Query("SELECT v FROM VoteDetail v WHERE v.todayExposed = true AND v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND v.eventEndAt >= :startOfToday AND v.eventEndAt < :startOfTomorrow "
            + "ORDER BY v.eventEndAt ASC")
    List<VoteDetail> findTodayExposed(@Param("now") LocalDateTime now,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);
}
