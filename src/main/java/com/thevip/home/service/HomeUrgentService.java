package com.thevip.home.service;

import com.thevip.global.config.CacheConfig;
import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 화면 긴급 배너의 소스는 MusicDetail/VoteDetail의 homeUrgent 플래그다.
 * 두 도메인 중 homeUrgent가 켜진 항목은 항상 하나여야 하는 불변식은
 * (지금은 없는) 어드민 토글 서비스가 강제하는 책임이며, 여기서는 조회만 한다.
 */
@Service
@RequiredArgsConstructor
public class HomeUrgentService {

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.HOME_URGENT)
    public Optional<HomeUrgentResponse> getCurrentUrgent() {
        Optional<HomeUrgentResponse> music = musicDetailRepository.findByHomeUrgentTrueAndActiveTrue().stream()
                .findFirst()
                .map(HomeUrgentResponse::fromMusic);
        if (music.isPresent()) {
            return music;
        }

        return voteDetailRepository.findByHomeUrgentTrueAndActiveTrue().stream()
                .findFirst()
                .map(HomeUrgentResponse::fromVote);
    }
}
