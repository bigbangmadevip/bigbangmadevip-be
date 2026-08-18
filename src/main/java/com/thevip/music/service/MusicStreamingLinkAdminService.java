package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.music.dto.MusicStreamingLinkAdminResponse;
import com.thevip.music.dto.MusicStreamingLinkBatchRequest;
import com.thevip.music.dto.StreamingPlatformResponse;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
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
    private final PlatformRepository platformRepository;

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
    // 응답은 공개 API(GET /api/v1/music/streaming)의 platforms[] 항목과 동일한 구조로 돌려줘서,
    // 화면에서 조회 응답을 그대로 편집해 재등록하는 흐름으로 쓸 수 있게 한다.
    @Transactional
    public StreamingPlatformResponse replaceForPlatform(Long platformId, MusicStreamingLinkBatchRequest request) {
        Platform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 플랫폼입니다."));

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

        return StreamingPlatformResponse.from(platform, links);
    }

    private MusicStreamingLink getEntity(Long id) {
        return musicStreamingLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 스트리밍 링크입니다."));
    }
}
