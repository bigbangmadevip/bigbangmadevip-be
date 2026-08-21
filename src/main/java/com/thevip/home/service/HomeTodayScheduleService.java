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
 * 홈 화면 "오늘의 총공 일정"의 소스는 MusicDetail/VoteDetail의 todayExposed 플래그이며,
 * 오늘 날짜에 해당하는 것만(음원: eventAt, 투표: eventEndAt) 시간순으로 최대 MAX_ITEMS개 노출한다.
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
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        List<MusicDetail> musicDetails = musicDetailRepository.findTodayExposed(now, startOfToday, startOfTomorrow);
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
