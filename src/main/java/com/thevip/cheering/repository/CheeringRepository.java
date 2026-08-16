package com.thevip.cheering.repository;

import com.thevip.cheering.entity.Cheering;
import com.thevip.cheering.entity.CheeringCategory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheeringRepository extends JpaRepository<Cheering, Long> {

    @Query("SELECT COUNT(DISTINCT c.memberId) FROM Cheering c WHERE c.cheeringDate = :date")
    long countDistinctMemberByCheeringDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT c.memberId) FROM Cheering c, CheeringItem i "
            + "WHERE c.itemId = i.id AND i.category = :category AND c.cheeringDate = :date")
    long countDistinctMemberByCategoryAndCheeringDate(
            @Param("category") CheeringCategory category, @Param("date") LocalDate date);

    @Query("SELECT c.itemId FROM Cheering c WHERE c.memberId = :memberId AND c.cheeringDate = :date")
    List<Long> findItemIdsByMemberIdAndCheeringDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    boolean existsByMemberIdAndItemIdAndCheeringDate(Long memberId, Long itemId, LocalDate date);

    // 마이페이지 "내 응원 기록" 요약용.
    long countByMemberId(Long memberId);

    @Query("SELECT COUNT(DISTINCT c.cheeringDate) FROM Cheering c WHERE c.memberId = :memberId")
    long countDistinctCheeringDateByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(DISTINCT c.cheeringDate) FROM Cheering c WHERE c.memberId = :memberId "
            + "AND c.cheeringDate BETWEEN :start AND :end")
    long countDistinctCheeringDateByMemberIdAndCheeringDateBetween(
            @Param("memberId") Long memberId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 참여 기록이 없으면 null.
    @Query("SELECT MIN(c.cheeringDate) FROM Cheering c WHERE c.memberId = :memberId")
    LocalDate findFirstCheeringDateByMemberId(@Param("memberId") Long memberId);

    // 마이페이지 응원 기록 캘린더용 (해당 월에 참여한 날짜 목록).
    @Query("SELECT DISTINCT c.cheeringDate FROM Cheering c WHERE c.memberId = :memberId "
            + "AND c.cheeringDate BETWEEN :start AND :end ORDER BY c.cheeringDate ASC")
    List<LocalDate> findDistinctCheeringDateByMemberIdAndCheeringDateBetween(
            @Param("memberId") Long memberId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 회원탈퇴 시 응원 기록도 함께 삭제한다 (Member를 FK 없이 ID로만 참조하므로 DB가 대신 지워주지 않는다).
    void deleteByMemberId(Long memberId);
}
