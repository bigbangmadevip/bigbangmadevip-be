package com.thevip.vote.service;

import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.MusicShow;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.MusicShowRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// MUSIC_SHOW 카테고리는 자체 platformIds(vote_detail_platform)가 아니라, 연결된 MusicShow의
// platformIds(music_show_platform)를 플랫폼 소스로 쓴다. musicShowId가 없는 나머지 카테고리는
// 기존처럼 VoteDetail 자체의 platformIds를 쓴다.
@Component
@RequiredArgsConstructor
public class VoteDetailPlatformResolver {

    private final MusicShowRepository musicShowRepository;
    private final PlatformRepository platformRepository;

    public List<String> resolveNames(VoteDetail detail) {
        List<Long> platformIds = detail.getMusicShowId() != null
                ? musicShowRepository.findById(detail.getMusicShowId())
                        .map(MusicShow::getPlatformIds).orElse(List.of())
                : detail.getPlatformIds();
        return platformRepository.findNamesByIds(platformIds);
    }
}
