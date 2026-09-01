package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.vote.dto.VoteDetailAdminRequest;
import com.thevip.vote.dto.VoteDetailAdminResponse;
import com.thevip.push.PushTopic;
import com.thevip.push.service.PushNotificationService;
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
    private final PushNotificationService pushNotificationService;

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
        detail.replacePlatformUrl(nullSafe(request.platformUrl()));
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
        applyPush(detail);
    }

    // 등록/수정 시점에 pushEnabled가 켜져 있으면 이전에 보낸 적이 있어도 다시 발송 대상이 된다
    // (이미 보낸 적 있는지는 응답의 pushSentAt으로 프론트가 보여주고, 그래도 켠 채로 저장하면 재발송).
    // 즉시발송(pushSendAt 없음)은 이 자리에서 바로 보내고, 예약발송은 발송기록만 초기화해서
    // 스케줄러가 (재)예약된 시각에 처리하게 한다.
    private void applyPush(VoteDetail detail) {
        if (!detail.isPushEnabled()) {
            return;
        }
        detail.resetPushSent();
        if (detail.getPushSendAt() == null) {
            pushNotificationService.send(PushTopic.VOTE, detail.isMenuUrgent(), detail.getPushTitle(), detail.getPushBody());
            detail.markPushSent();
        }
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
