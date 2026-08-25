package com.thevip.home.service;

import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 화면 "오늘의 총공 일정"은 MusicDetail/VoteDetail 중 오늘 노출 대상인 것만(findTodayExposed)
 * 마감(eventEndAt) 임박순으로 최대 MAX_ITEMS개 노출한다. 음원/투표 둘 다 "마감"이라는 동일한
 * 기준으로 비교하기 때문에(HomeScheduleItemResponse.fromMusic/fromVote 참고) 날짜까지 그대로
 * 비교해도 된다 — 시작일이 며칠 전이어도 상관없이, 언제 사라지는지만 보면 되기 때문이다.
 */
@Service
@RequiredArgsConstructor
public class HomeTodayScheduleService {

    private static final int MAX_ITEMS = 5;

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;
    private final VoteDetailPlatformResolver voteDetailPlatformResolver;

    @Transactional(readOnly = true)
    public List<HomeScheduleItemResponse> getTodaySchedule() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        List<MusicDetail> musicDetails = musicDetailRepository.findTodayExposed(now, startOfTomorrow);
        List<VoteDetail> voteDetails = voteDetailRepository.findTodayExposed(now, startOfTomorrow);

        Stream<HomeScheduleItemResponse> musicItems = musicDetails.stream()
                .map(detail -> HomeScheduleItemResponse.fromMusic(detail,
                        platformRepository.findNamesByIds(detail.getPlatformIds())));
        Stream<HomeScheduleItemResponse> voteItems = voteDetails.stream()
                .map(detail -> HomeScheduleItemResponse.fromVote(detail,
                        voteDetailPlatformResolver.resolveNames(detail)));

        return Stream.concat(musicItems, voteItems)
                .sorted(Comparator.comparing(HomeScheduleItemResponse::time))
                .limit(MAX_ITEMS)
                .toList();
    }
}
