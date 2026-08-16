package com.thevip.music.repository;

import com.thevip.music.entity.MusicDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MusicDetailRepository extends JpaRepository<MusicDetail, Long> {

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // eventAt이 오늘 날짜가 지나면(총공 당일이 지나면) 어드민이 안 끄더라도 배너에서 자동으로 빠진다.
    @Query("SELECT m FROM MusicDetail m WHERE m.menuUrgent = true AND m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND m.eventAt >= :startOfToday AND m.eventAt < :startOfTomorrow")
    List<MusicDetail> findVisibleMenuUrgent(@Param("now") LocalDateTime now,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // eventAt이 오늘 날짜인 것만 "오늘의 총공 일정"에 노출한다.
    @Query("SELECT m FROM MusicDetail m WHERE m.todayExposed = true AND m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND m.eventAt >= :startOfToday AND m.eventAt < :startOfTomorrow "
            + "ORDER BY m.eventAt ASC")
    List<MusicDetail> findTodayExposed(@Param("now") LocalDateTime now,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    // "일정" 탭 캘린더/일별 리스트용. todayExposed와 무관하게 활성 상태인 항목 전부를 대상으로 하고,
    // 지난 일정도 조회할 수 있어야 해서 만료 여부는 걸러내지 않는다.
    @Query("SELECT m FROM MusicDetail m WHERE m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND m.eventAt >= :rangeStart AND m.eventAt < :rangeEnd "
            + "ORDER BY m.eventAt ASC")
    List<MusicDetail> findActiveInRange(@Param("now") LocalDateTime now,
            @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);
}
