package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.music.dto.MusicDetailAdminRequest;
import com.thevip.music.dto.MusicDetailAdminResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicDetailAdminService {

    private final MusicDetailRepository musicDetailRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public List<MusicDetailAdminResponse> list() {
        return musicDetailRepository.findAll().stream()
                .sorted(Comparator.comparing(MusicDetail::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicDetailAdminResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public MusicDetailAdminResponse create(MusicDetailAdminRequest request) {
        MusicDetail detail = MusicDetail.of(request.category(), request.title(), request.songName(),
                request.eventStartAt(), request.eventEndAt());
        musicDetailRepository.save(detail);
        applyRequest(detail, request);
        return toResponse(detail);
    }

    @Transactional
    public MusicDetailAdminResponse update(Long id, MusicDetailAdminRequest request) {
        MusicDetail detail = getEntity(id);
        detail.updateCore(request.category(), request.title(), request.songName(), request.eventStartAt(),
                request.eventEndAt());
        applyRequest(detail, request);
        return toResponse(detail);
    }

    private void applyRequest(MusicDetail detail, MusicDetailAdminRequest request) {
        detail.replacePlatformIds(platformRepository.resolveIdsByCodes(nullSafe(request.platformCodes())));
        detail.replaceChecklist(nullSafe(request.checklist()));
        detail.replaceImageUrls(nullSafe(request.imageUrls()));
        detail.replaceGuideIds(nullSafe(request.guideIds()));
        detail.updateUrgentContent(request.urgentContent());
        detail.updateActive(request.active());
        detail.updateScheduledAt(request.scheduledAt());
        applyMenuUrgent(detail, request.menuUrgent());
    }

    // 메뉴(음원)당 긴급 배너는 최대 1개만 켜져 있어야 하는 불변식을 여기서 강제한다.
    private void applyMenuUrgent(MusicDetail detail, boolean menuUrgent) {
        if (menuUrgent) {
            musicDetailRepository.findByMenuUrgentTrue().stream()
                    .filter(other -> !other.getId().equals(detail.getId()))
                    .forEach(other -> other.updateMenuUrgent(false));
        }
        detail.updateMenuUrgent(menuUrgent);
    }

    private MusicDetailAdminResponse toResponse(MusicDetail detail) {
        return MusicDetailAdminResponse.from(detail, platformRepository.findCodesByIds(detail.getPlatformIds()));
    }

    private MusicDetail getEntity(Long id) {
        return musicDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 음원 상세입니다."));
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
