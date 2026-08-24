package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.vote.dto.VoteDetailAdminRequest;
import com.thevip.vote.dto.VoteDetailAdminResponse;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteDetailAdminService {

    private final VoteDetailRepository voteDetailRepository;

    @Transactional(readOnly = true)
    public List<VoteDetailAdminResponse> list() {
        return voteDetailRepository.findAll().stream()
                .sorted(Comparator.comparing(VoteDetail::getCreatedAt).reversed())
                .map(VoteDetailAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoteDetailAdminResponse get(Long id) {
        return VoteDetailAdminResponse.from(getEntity(id));
    }

    @Transactional
    public VoteDetailAdminResponse create(VoteDetailAdminRequest request) {
        VoteDetail detail = VoteDetail.of(request.category(), request.title(), request.rewardDescription(),
                request.eventStartAt(), request.eventEndAt());
        voteDetailRepository.save(detail);
        applyRequest(detail, request);
        return VoteDetailAdminResponse.from(detail);
    }

    @Transactional
    public VoteDetailAdminResponse update(Long id, VoteDetailAdminRequest request) {
        VoteDetail detail = getEntity(id);
        detail.updateCore(request.category(), request.title(), request.rewardDescription(),
                request.eventStartAt(), request.eventEndAt());
        applyRequest(detail, request);
        return VoteDetailAdminResponse.from(detail);
    }

    private void applyRequest(VoteDetail detail, VoteDetailAdminRequest request) {
        detail.updateMusicShowId(request.musicShowId());
        detail.replacePlatformIds(nullSafe(request.platformIds()));
        detail.updatePlatformUrl(request.platformUrl());
        detail.replaceChecklist(nullSafe(request.checklist()));
        detail.replaceImageUrls(nullSafe(request.imageUrls()));
        detail.replaceGuideIds(nullSafe(request.guideIds()));
        detail.updateCtaButtonLabel(request.ctaButtonLabel());
        detail.updateUrgentContent(request.urgentContent());
        detail.updateActive(request.active());
        detail.updateScheduledAt(request.scheduledAt());
        detail.updatePushEnabled(request.pushEnabled());
        detail.updatePushSendAt(request.pushSendAt());
        detail.updatePushTitle(request.pushTitle());
        detail.updatePushBody(request.pushBody());
        applyMenuUrgent(detail, request.menuUrgent());
    }

    // 메뉴(투표)당 긴급 배너는 최대 1개만 켜져 있어야 하는 불변식을 여기서 강제한다.
    private void applyMenuUrgent(VoteDetail detail, boolean menuUrgent) {
        if (menuUrgent) {
            voteDetailRepository.findByMenuUrgentTrue().stream()
                    .filter(other -> !other.getId().equals(detail.getId()))
                    .forEach(other -> other.updateMenuUrgent(false));
        }
        detail.updateMenuUrgent(menuUrgent);
    }

    private VoteDetail getEntity(Long id) {
        return voteDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 투표 상세입니다."));
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
