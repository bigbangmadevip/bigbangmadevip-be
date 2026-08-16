package com.thevip.notice.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.notice.dto.NoticeDetailResponse;
import com.thevip.notice.dto.NoticeListItemResponse;
import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<NoticeListItemResponse> getNotices(NoticeMenuType menuType) {
        return noticeRepository.findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(menuType).stream()
                .map(NoticeListItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNotice(NoticeMenuType menuType, Long id) {
        Notice notice = noticeRepository.findByIdAndMenuType(id, menuType)
                .filter(Notice::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 공지입니다."));
        return NoticeDetailResponse.from(notice);
    }
}
