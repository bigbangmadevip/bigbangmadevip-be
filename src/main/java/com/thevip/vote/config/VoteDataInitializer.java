package com.thevip.vote.config;

import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (voteDetailRepository.count() > 0) {
            return;
        }

        // homeUrgent는 기본값 false 유지 - 지금은 음원 쪽만 홈 배너로 노출
        VoteDetail detail = VoteDetail.of(
                "인기가요 생방송 투표", null, "하이어(Higher)", null,
                LocalDateTime.of(2026, 7, 30, 23, 59));
        voteDetailRepository.save(detail);
    }
}
