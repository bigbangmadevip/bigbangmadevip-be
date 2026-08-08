package com.thevip.home.service;

import com.thevip.global.config.CacheConfig;
import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.HOME_TODAY_SCHEDULE)
    public List<HomeScheduleItemResponse> getTodaySchedule() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        List<MusicDetail> musicDetails = musicDetailRepository.findTodayExposed(now, startOfToday, startOfTomorrow);
        List<VoteDetail> voteDetails = voteDetailRepository.findTodayExposed(now);

        Stream<HomeScheduleItemResponse> musicItems = musicDetails.stream()
                .map(detail -> HomeScheduleItemResponse.fromMusic(detail, platformNames(detail.getPlatformIds())));
        Stream<HomeScheduleItemResponse> voteItems = voteDetails.stream()
                .map(detail -> HomeScheduleItemResponse.fromVote(detail, platformNames(detail.getPlatformIds())));

        return Stream.concat(musicItems, voteItems)
                .sorted(Comparator.comparing(HomeScheduleItemResponse::time))
                .limit(MAX_ITEMS)
                .toList();
    }

    private List<String> platformNames(List<Long> platformIds) {
        // @OrderColumn 컬렉션은 sort_order가 연속이 아니면 빈 인덱스를 null로 채운다
        // (수동 SQL로 데이터를 넣을 때 흔히 발생). null은 걸러내고 진행한다.
        return platformIds.stream()
                .filter(Objects::nonNull)
                .map(platformRepository::findById)
                .flatMap(Optional::stream)
                .map(Platform::getName)
                .toList();
    }
}
