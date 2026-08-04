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

    @Query("SELECT COUNT(DISTINCT c.memberId) FROM Cheering c "
            + "WHERE c.itemId = :itemId AND c.cheeringDate = :date")
    long countDistinctMemberByItemIdAndCheeringDate(@Param("itemId") Long itemId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT c.memberId) FROM Cheering c, CheeringItem i "
            + "WHERE c.itemId = i.id AND i.category = :category AND c.cheeringDate = :date")
    long countDistinctMemberByCategoryAndCheeringDate(
            @Param("category") CheeringCategory category, @Param("date") LocalDate date);

    @Query("SELECT c.itemId FROM Cheering c WHERE c.memberId = :memberId AND c.cheeringDate = :date")
    List<Long> findItemIdsByMemberIdAndCheeringDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    boolean existsByMemberIdAndItemIdAndCheeringDate(Long memberId, Long itemId, LocalDate date);
}
