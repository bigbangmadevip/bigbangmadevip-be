package com.thevip.home.dto;

import com.thevip.vote.entity.VoteDetail;
import java.util.List;
import java.util.Objects;

// platformNames/platformUrl은 서로 인덱스로 짝지어진 값이 아니라 각각 독립된 목록이다
// (VoteDetail.platformIds/platformUrl 참고).
public record HomeVoteUrlResponse(
        Long id,
        String title,
        List<String> platformNames,
        List<String> platformUrl) {

    public static HomeVoteUrlResponse from(VoteDetail detail, List<String> platformNames) {
        // 트랜잭션 안에서(지연 로딩 가능한 시점에) 바로 리스트로 굳혀야 컨트롤러에서 직렬화할 때
        // LazyInitializationException이 안 난다 (VoteDetailResponse.from과 동일한 패턴).
        return new HomeVoteUrlResponse(detail.getId(), detail.getTitle(), platformNames,
                detail.getPlatformUrl().stream().filter(Objects::nonNull).toList());
    }
}
