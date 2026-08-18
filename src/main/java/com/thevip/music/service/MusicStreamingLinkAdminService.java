package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.music.dto.MusicStreamingLinkAdminResponse;
import com.thevip.music.dto.MusicStreamingLinkBatchRequest;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import java.util.ArrayList;
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

    // 플랫폼 하나의 원클릭 스트리밍 링크 전체를 통째로 교체한다. 화면이 플랫폼 -> 운영체제 -> 링크 순서
    // 리스트 단위로 관리하지, 링크 하나씩 관리하지 않기 때문에 기존 행은 다 지우고 요청대로 다시 만든다.
    @Transactional
    public List<MusicStreamingLinkAdminResponse> replaceForPlatform(Long platformId,
            MusicStreamingLinkBatchRequest request) {
        musicStreamingLinkRepository.deleteByPlatformId(platformId);

        List<MusicStreamingLink> links = new ArrayList<>();
        for (MusicStreamingLinkBatchRequest.OsGroupRequest group : request.osGroups()) {
            List<MusicStreamingLinkBatchRequest.LinkRequest> groupLinks = group.links();
            for (int i = 0; i < groupLinks.size(); i++) {
                MusicStreamingLinkBatchRequest.LinkRequest linkRequest = groupLinks.get(i);
                links.add(MusicStreamingLink.of(platformId, group.os(), linkRequest.label(), linkRequest.url(), i));
            }
        }
        musicStreamingLinkRepository.saveAll(links);

        return links.stream().map(MusicStreamingLinkAdminResponse::from).toList();
    }

    private MusicStreamingLink getEntity(Long id) {
        return musicStreamingLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 스트리밍 링크입니다."));
    }
}
