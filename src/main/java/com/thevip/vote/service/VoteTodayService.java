package com.thevip.vote.service;

import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.dto.VoteSummaryResponse;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.dto.VoteUrgentResponse;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final int DUE_SOON_LIMIT = 4;

    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public VoteTodayResponse getToday() {
        LocalDateTime now = LocalDateTime.now();

        VoteUrgentResponse urgent = voteDetailRepository.findVisibleMenuUrgent(now)
                .stream().findFirst()
                .map(VoteUrgentResponse::from)
                .orElse(null);

        // findActiveOngoing은 eventEndAt 오름차순이라 마감 임박(24시간 이내) 항목이 항상 앞쪽에 몰려 있다.
        List<VoteDetail> ongoing = voteDetailRepository.findActiveOngoing(now);
        LocalDateTime dueSoonCutoff = now.plusHours(24);

        List<VoteDetail> dueSoon = ongoing.stream()
                .filter(detail -> !detail.getEventEndAt().isAfter(dueSoonCutoff))
                .limit(DUE_SOON_LIMIT)
                .toList();

        List<VoteDetail> rest = new ArrayList<>(ongoing);
        rest.removeAll(dueSoon);

        return new VoteTodayResponse(urgent, toSummaries(dueSoon), toSummaries(rest));
    }

    private List<VoteSummaryResponse> toSummaries(List<VoteDetail> details) {
        return details.stream()
                .map(detail -> VoteSummaryResponse.from(detail, platformRepository.findNamesByIds(detail.getPlatformIds())))
                .toList();
    }
}
