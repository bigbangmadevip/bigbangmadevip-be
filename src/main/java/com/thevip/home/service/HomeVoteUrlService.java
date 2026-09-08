package com.thevip.home.service;

import com.thevip.home.dto.HomeVoteUrlResponse;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 홈 화면 "오늘의 응원" 카드에 노출할, 지금 진행 중인 음악방송(MUSIC_SHOW) 투표의 제목/플랫폼/URL.
@Service
@RequiredArgsConstructor
public class HomeVoteUrlService {

    private final VoteDetailRepository voteDetailRepository;
    private final VoteDetailPlatformResolver voteDetailPlatformResolver;

    @Transactional(readOnly = true)
    public List<HomeVoteUrlResponse> getOngoingMusicShowVoteUrls() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        return voteDetailRepository.findActiveOngoingByCategory(VoteCategory.MUSIC_SHOW, now, startOfTomorrow)
                .stream()
                .map(detail -> HomeVoteUrlResponse.from(detail, voteDetailPlatformResolver.resolveNames(detail)))
                .toList();
    }
}
