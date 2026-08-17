package com.thevip.notice.repository;

import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeMenuType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 고정된 공지가 먼저, 그다음 최신순.
    List<Notice> findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(NoticeMenuType menuType);

    // 어드민 목록 조회용 - 비활성 공지도 함께 보여준다.
    List<Notice> findByMenuTypeOrderByPinnedDescCreatedAtDesc(NoticeMenuType menuType);

    Optional<Notice> findByIdAndMenuType(Long id, NoticeMenuType menuType);
}
