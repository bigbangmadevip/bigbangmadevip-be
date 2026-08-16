package com.thevip.home.service;

import com.thevip.global.config.CacheConfig;
import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 화면 긴급 배너의 소스는 MusicDetail/VoteDetail의 menuUrgent 플래그다 (메뉴당 최대 하나, 없을 수도 있음).
 * 이 불변식(메뉴당 최대 하나)은 어드민 서비스가 강제하는 책임이며, 여기서는 조회 후
 * 음원/투표 후보가 둘 다 있으면 날짜(eventAt/eventEndAt)가 더 임박한 쪽을 고르기만 한다.
 */
@Service
@RequiredArgsConstructor
public class HomeUrgentService {

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.HOME_URGENT)
    public Optional<HomeUrgentResponse> getCurrentUrgent() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = startOfToday.plusDays(1);

        Optional<MusicDetail> music = musicDetailRepository
                .findVisibleMenuUrgent(now, startOfToday, startOfTomorrow)
                .stream().findFirst();
        Optional<VoteDetail> vote = voteDetailRepository.findVisibleMenuUrgent(now)
                .stream().findFirst();

        if (music.isPresent() && vote.isPresent()) {
            return isMusicSooner(music.get(), vote.get())
                    ? music.map(this::toResponse)
                    : vote.map(this::toResponse);
        }
        if (music.isPresent()) {
            return music.map(this::toResponse);
        }
        return vote.map(this::toResponse);
    }

    private HomeUrgentResponse toResponse(MusicDetail detail) {
        return HomeUrgentResponse.fromMusic(detail, platformRepository.findNamesByIds(detail.getPlatformIds()));
    }

    private HomeUrgentResponse toResponse(VoteDetail detail) {
        return HomeUrgentResponse.fromVote(detail, platformRepository.findNamesByIds(detail.getPlatformIds()));
    }

    // 날짜가 없는 쪽은 비교 대상에서 밀려난다: 둘 다 없으면 투표를, 한쪽만 있으면 그쪽을 우선한다.
    private boolean isMusicSooner(MusicDetail music, VoteDetail vote) {
        LocalDateTime musicAt = music.getEventAt();
        LocalDateTime voteAt = vote.getEventEndAt();
        if (musicAt == null) {
            return false;
        }
        if (voteAt == null) {
            return true;
        }
        return musicAt.isBefore(voteAt);
    }
}
