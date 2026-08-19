package com.thevip.notice.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.notice.dto.NoticeAdminRequest;
import com.thevip.notice.dto.NoticeAdminResponse;
import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeLink;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.repository.NoticeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeAdminService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public List<NoticeAdminResponse> list(NoticeMenuType menuType) {
        return noticeRepository.findByMenuTypeOrderByPinnedDescCreatedAtDesc(menuType).stream()
                .map(NoticeAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeAdminResponse get(NoticeMenuType menuType, Long id) {
        return NoticeAdminResponse.from(getEntity(menuType, id));
    }

    @Transactional
    public NoticeAdminResponse create(NoticeMenuType menuType, NoticeAdminRequest request, String updatedBy) {
        Notice notice = Notice.of(menuType, request.title(), request.content());
        noticeRepository.save(notice);
        applyRequest(notice, request, updatedBy);
        return NoticeAdminResponse.from(notice);
    }

    @Transactional
    public NoticeAdminResponse update(NoticeMenuType menuType, Long id, NoticeAdminRequest request,
            String updatedBy) {
        Notice notice = getEntity(menuType, id);
        applyRequest(notice, request, updatedBy);
        return NoticeAdminResponse.from(notice);
    }

    private void applyRequest(Notice notice, NoticeAdminRequest request, String updatedBy) {
        notice.update(request.title(), request.content(), updatedBy);
        notice.replaceImageUrls(request.imageUrls() == null ? List.of() : request.imageUrls());
        List<NoticeLink> links = request.links() == null
                ? List.of()
                : request.links().stream().map(link -> new NoticeLink(link.label(), link.url())).toList();
        notice.replaceLinks(links);
        notice.updatePinned(request.pinned());
        notice.updateActive(request.active());
    }

    private Notice getEntity(NoticeMenuType menuType, Long id) {
        return noticeRepository.findByIdAndMenuType(id, menuType)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 공지입니다."));
    }
}
