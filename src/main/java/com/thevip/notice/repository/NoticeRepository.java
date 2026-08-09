package com.thevip.notice.repository;

import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeMenuType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByMenuTypeAndActiveTrueOrderByCreatedAtDesc(NoticeMenuType menuType);

    Optional<Notice> findByIdAndMenuType(Long id, NoticeMenuType menuType);
}
