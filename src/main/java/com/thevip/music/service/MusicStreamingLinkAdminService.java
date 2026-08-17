package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.music.dto.MusicStreamingLinkAdminRequest;
import com.thevip.music.dto.MusicStreamingLinkAdminResponse;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicStreamingLinkAdminService {

    private final MusicStreamingLinkRepository musicStreamingLinkRepository;

    @Transactional(readOnly = true)
    public List<MusicStreamingLinkAdminResponse> list() {
        return musicStreamingLinkRepository.findAll().stream()
                .sorted(Comparator.comparing(MusicStreamingLink::getPlatformId,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(MusicStreamingLink::getSortOrder))
                .map(MusicStreamingLinkAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicStreamingLinkAdminResponse get(Long id) {
        return MusicStreamingLinkAdminResponse.from(getEntity(id));
    }

    @Transactional
    public MusicStreamingLinkAdminResponse create(MusicStreamingLinkAdminRequest request) {
        MusicStreamingLink link = MusicStreamingLink.of(request.platformId(), request.os(), request.label(),
                request.url(), request.sortOrder());
        link.updateActive(request.active());
        musicStreamingLinkRepository.save(link);
        return MusicStreamingLinkAdminResponse.from(link);
    }

    @Transactional
    public MusicStreamingLinkAdminResponse update(Long id, MusicStreamingLinkAdminRequest request) {
        MusicStreamingLink link = getEntity(id);
        link.update(request.platformId(), request.os(), request.label(), request.url(), request.sortOrder());
        link.updateActive(request.active());
        return MusicStreamingLinkAdminResponse.from(link);
    }

    private MusicStreamingLink getEntity(Long id) {
        return musicStreamingLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 스트리밍 링크입니다."));
    }
}
