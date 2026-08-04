package com.thevip.cheering.repository;

import com.thevip.cheering.entity.CheeringItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheeringItemRepository extends JpaRepository<CheeringItem, Long> {

    List<CheeringItem> findByActiveTrueOrderBySortOrder();
}
