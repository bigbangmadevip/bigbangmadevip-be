package com.thevip.music.repository;

import com.thevip.music.entity.MusicDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MusicDetailRepository extends JpaRepository<MusicDetail, Long> {

    // 메뉴(음원)당 긴급 배너는 최대 1개만 켜져 있어야 하는 불변식을 어드민 서비스가 강제할 때 쓴다.
    List<MusicDetail> findByMenuUrgentTrue();

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // 총공은 [eventStartAt, eventEndAt] 구간 안에 있을 때만 노출한다. eventStartAt은 시각까지가
    // 아니라 날짜만 본다 — 시작일이 오늘이면 몇 시로 등록했든 오늘 0시부터 바로 노출된다
    // (startOfTomorrow 이전이면 통과). eventStartAt이 없으면 시작 제약 없음으로 본다.
    @Query("SELECT m FROM MusicDetail m WHERE m.menuUrgent = true AND m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND (m.eventStartAt IS NULL OR m.eventStartAt < :startOfTomorrow) "
            + "AND m.eventEndAt >= :now")
    List<MusicDetail> findVisibleMenuUrgent(@Param("now") LocalDateTime now,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // findVisibleMenuUrgent와 동일한 규칙(eventStartAt 날짜만 확인, eventEndAt 지나면 제외)으로
    // "오늘의 총공 일정"에 노출한다.
    @Query("SELECT m FROM MusicDetail m WHERE m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND (m.eventStartAt IS NULL OR m.eventStartAt < :startOfTomorrow) "
            + "AND m.eventEndAt >= :now "
            + "ORDER BY m.eventStartAt ASC")
    List<MusicDetail> findTodayExposed(@Param("now") LocalDateTime now,
            @Param("startOfTomorrow") LocalDateTime startOfTomorrow);

    // "일정" 탭 캘린더/일별 리스트용. todayExposed와 무관하게 활성 상태인 항목 전부를 대상으로 하고,
    // 지난 일정도 조회할 수 있어야 해서 만료 여부는 걸러내지 않는다. 시작일(eventStartAt) 기준으로
    // 하루에만 노출한다(투표처럼 기간 전체에 매일 걸쳐 노출하지는 않는다).
    @Query("SELECT m FROM MusicDetail m WHERE m.active = true "
            + "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) "
            + "AND m.eventStartAt >= :rangeStart AND m.eventStartAt < :rangeEnd "
            + "ORDER BY m.eventStartAt ASC")
    List<MusicDetail> findActiveInRange(@Param("now") LocalDateTime now,
            @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);
}
