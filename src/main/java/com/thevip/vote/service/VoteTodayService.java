package com.thevip.vote.service;

import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.dto.VoteSummaryResponse;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.dto.VoteUrgentResponse;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 투표 메뉴 "오늘의 투표" 탭: 긴급 총공 배너(투표만, menuUrgent) + 지금 진행 중인 투표 전부를 조립한다.
 */
@Service
@RequiredArgsConstructor
public class VoteTodayService {

    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public VoteTodayResponse getToday() {
        LocalDateTime now = LocalDateTime.now();

        VoteUrgentResponse urgent = voteDetailRepository.findVisibleMenuUrgent(now)
                .stream().findFirst()
                .map(VoteUrgentResponse::from)
                .orElse(null);

        List<VoteSummaryResponse> votes = voteDetailRepository.findActiveOngoing(now).stream()
                .map(detail -> VoteSummaryResponse.from(detail, platformRepository.findNamesByIds(detail.getPlatformIds())))
                .toList();

        return new VoteTodayResponse(urgent, votes);
    }
}
