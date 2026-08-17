package com.thevip.vote.repository;

import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteDetailRepository extends JpaRepository<VoteDetail, Long> {

    // 메뉴(투표)당 긴급 배너는 최대 1개만 켜져 있어야 하는 불변식을 어드민 서비스가 강제할 때 쓴다.
    List<VoteDetail> findByMenuUrgentTrue();

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // eventEndAt이 지나면(마감되면) 어드민이 안 끄더라도 배너에서 자동으로 빠진다.
    @Query("SELECT v FROM VoteDetail v WHERE v.menuUrgent = true AND v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND v.eventEndAt >= :now")
    List<VoteDetail> findVisibleMenuUrgent(@Param("now") LocalDateTime now);

    // active=true여도 scheduledAt이 미래면 아직 예약 대기중이라 제외한다 (배치 없이 조회 시점 계산).
    // 투표는 마감(eventEndAt)이 지나기 전까지 매일 노출한다 (음원처럼 특정 날짜 1회성 이벤트가 아니라
    // 마감까지 계속 진행되는 액션이라 "오늘 날짜인 것만"이 아니라 "아직 마감 안 지난 것"으로 판단한다).
    // todayExposed 필드는 더 이상 이 조회에서 쓰지 않는다 — 게시 여부(active)만으로 노출을
    // 판단한다 (필드 자체는 남겨둠).
    @Query("SELECT v FROM VoteDetail v WHERE v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND v.eventEndAt >= :now "
            + "ORDER BY v.eventEndAt ASC")
    List<VoteDetail> findTodayExposed(@Param("now") LocalDateTime now);

    // "일정" 탭 캘린더/일별 리스트용(EVERY_DAY 모드). todayExposed와 무관하게 활성 상태인 항목 전부를
    // 대상으로 하고, 지난 일정도 조회할 수 있어야 해서 만료 여부는 걸러내지 않는다. 투표는
    // [eventStartAt(없으면 scheduledAt, 그것도 없으면 createdAt), eventEndAt] 구간을 "진행 중"으로 보고,
    // 이 구간이 요청한 [rangeStart, rangeEnd) 범위와 겹치는 항목을 반환한다
    // (구간을 날짜별로 펼치는 건 서비스에서).
    @Query("SELECT v FROM VoteDetail v WHERE v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND COALESCE(v.eventStartAt, v.scheduledAt, v.createdAt) < :rangeEnd "
            + "AND v.eventEndAt >= :rangeStart "
            + "ORDER BY v.eventEndAt ASC")
    List<VoteDetail> findActiveOverlapping(@Param("now") LocalDateTime now,
            @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);

    // "일정" 탭 캘린더/일별 리스트용(DEADLINE_ONLY 모드). 시작일과 무관하게 마감일(eventEndAt) 하루에만
    // 노출하고 싶을 때 쓴다. MusicDetail.findActiveInRange와 동일한 형태(단일 시점 기준 범위 조회).
    @Query("SELECT v FROM VoteDetail v WHERE v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND v.eventEndAt >= :rangeStart AND v.eventEndAt < :rangeEnd "
            + "ORDER BY v.eventEndAt ASC")
    List<VoteDetail> findActiveByDeadlineInRange(@Param("now") LocalDateTime now,
            @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);

    // 투표 메뉴 "오늘의 투표" 탭용. todayExposed(홈 전용 개념)와 무관하게, 지금 진행 중인(마감 안 지난)
    // 투표 전부를 마감 임박순으로 반환한다.
    @Query("SELECT v FROM VoteDetail v WHERE v.active = true "
            + "AND (v.scheduledAt IS NULL OR v.scheduledAt <= :now) "
            + "AND v.eventEndAt >= :now "
            + "ORDER BY v.eventEndAt ASC")
    List<VoteDetail> findActiveOngoing(@Param("now") LocalDateTime now);
}
