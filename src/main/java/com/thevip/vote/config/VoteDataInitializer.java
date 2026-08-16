package com.thevip.vote.config;

import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 투표 상세 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class VoteDataInitializer implements ApplicationRunner {

    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (voteDetailRepository.count() > 0) {
            return;
        }

        Platform platform = platformRepository.findByName("하이어(Higher)")
                .orElseGet(() -> platformRepository.save(
                        Platform.of("하이어(Higher)", PlatformType.VOTE, PlatformRegion.DOMESTIC, null)));

        // menuUrgent는 기본값 false 유지 - 지금은 음원 쪽만 긴급 배너 후보
        VoteDetail detail = VoteDetail.of(
                VoteCategory.MUSIC_SHOW, "인기가요 생방송 투표", "1위 시 팬사인회 진행",
                null, LocalDate.now().atTime(23, 59), 0);
        detail.addPlatformId(platform.getId());
        detail.addChecklistItem("하루 최대 투표 가능 횟수 확인");
        detail.updatePlatformUrl("https://example.com/vote");
        detail.updateCtaButtonLabel("투표하러 가기");
        detail.updateTodayExposed(true);
        voteDetailRepository.save(detail);
    }
}
