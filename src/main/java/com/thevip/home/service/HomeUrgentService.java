package com.thevip.home.service;

import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 화면 긴급 배너의 소스는 MusicDetail/VoteDetail의 menuUrgent 플래그다 (메뉴당 최대 하나, 없을 수도 있음).
 * 이 불변식(메뉴당 최대 하나)은 어드민 서비스가 강제하는 책임이며, 여기서는 음원/투표 후보를
 * 하나만 골라서 보여주지 않고 있는 대로 각각 반환한다(둘 다 없으면 빈 리스트).
 */
@Service
@RequiredArgsConstructor
public class HomeUrgentService {

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;
    private final VoteDetailPlatformResolver voteDetailPlatformResolver;

    @Transactional(readOnly = true)
    public List<HomeUrgentResponse> getCurrentUrgent() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();

        List<HomeUrgentResponse> result = new ArrayList<>();
        musicDetailRepository.findVisibleMenuUrgent(now, startOfTomorrow).stream().findFirst()
                .map(detail -> HomeUrgentResponse.fromMusic(detail,
                        platformRepository.findNamesByIds(detail.getPlatformIds())))
                .ifPresent(result::add);
        voteDetailRepository.findVisibleMenuUrgent(now).stream().findFirst()
                .map(detail -> HomeUrgentResponse.fromVote(detail, voteDetailPlatformResolver.resolveNames(detail)))
                .ifPresent(result::add);

        // 마감 임박순 정렬. eventEndAt이 같으면 정렬이 안정적(stable)이라 위에서 먼저 추가한
        // 음원이 그대로 앞에 남는다.
        return result.stream()
                .sorted(Comparator.comparing(HomeUrgentResponse::eventEndAt))
                .toList();
    }
}
