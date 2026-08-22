package com.thevip.vote.service;

import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteDetail;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 투표 플랫폼은 중분류(MusicShow) 단위가 아니라 상세(VoteDetail)마다 다를 수 있다
// (예: 이번 주 음악중심은 뮤빗만, 다음 주는 뮤빗+뮤니버스). 그래서 항상 상세 자체의
// platformIds(vote_detail_platform)를 플랫폼 소스로 쓴다.
@Component
@RequiredArgsConstructor
public class VoteDetailPlatformResolver {

    private final PlatformRepository platformRepository;

    public List<String> resolveNames(VoteDetail detail) {
        return platformRepository.findNamesByIds(detail.getPlatformIds());
    }
}
