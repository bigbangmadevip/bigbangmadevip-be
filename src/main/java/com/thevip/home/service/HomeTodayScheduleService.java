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
 * 시:분(time-of-day) 기준으로 최대 MAX_ITEMS개 노출한다. 날짜는 비교하지 않는다 — 여러 날에
 * 걸친 총공/투표는 시작·마감 날짜가 오늘이 아닐 수 있어서, 날짜까지 비교하면 순서가 뒤틀린다.
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

        // 날짜(day)는 무시하고 시:분(time-of-day)만 비교한다 — 이미 findTodayExposed로 "오늘 노출
        // 대상"만 걸러진 상태라, 여러 날에 걸친 총공/투표의 시작·마감 날짜가 오늘이 아닐 수 있어서
        // (예: 며칠 전 시작한 총공, 며칠 뒤 마감하는 투표) 날짜까지 비교하면 순서가 뒤틀린다.
        return Stream.concat(musicItems, voteItems)
                .sorted(Comparator.comparing(item -> item.time().toLocalTime()))
                .limit(MAX_ITEMS)
                .toList();
    }
}
