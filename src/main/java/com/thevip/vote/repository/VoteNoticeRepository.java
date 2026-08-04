package com.thevip.vote.repository;

import com.thevip.vote.entity.VoteNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteNoticeRepository extends JpaRepository<VoteNotice, Long> {
}
