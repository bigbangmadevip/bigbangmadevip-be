package com.thevip.vote.repository;

import com.thevip.vote.entity.VoteDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteDetailRepository extends JpaRepository<VoteDetail, Long> {

    List<VoteDetail> findByMenuUrgentTrueAndActiveTrue();
}
