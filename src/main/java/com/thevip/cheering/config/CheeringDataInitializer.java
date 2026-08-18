package com.thevip.cheering.config;

import com.thevip.cheering.entity.CheeringItem;
import com.thevip.cheering.repository.CheeringItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 응원 항목 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class CheeringDataInitializer implements ApplicationRunner {

    private final CheeringItemRepository cheeringItemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (cheeringItemRepository.count() > 0) {
            return;
        }

        cheeringItemRepository.save(CheeringItem.of("STREAMING", "음원\n스트리밍", "봄여름가을겨울", 0));
        cheeringItemRepository.save(CheeringItem.of("VOTE", "인기가요\n사전 투표", null, 1));
        cheeringItemRepository.save(CheeringItem.of("VOTE", "멜론 주간인기상\n투표", null, 2));
        cheeringItemRepository.save(CheeringItem.of("YOUTUBE", "유튜브\n뮤직비디오 조회", "봄여름가을겨울", 3));
        cheeringItemRepository.save(CheeringItem.of("REPORT", "네이버 기사\n댓글 작성", null, 4));
        cheeringItemRepository.save(CheeringItem.of("DOWNLOAD", "선착순\n이벤트 참여", null, 5));
        cheeringItemRepository.save(CheeringItem.of("HASHTAG", "해시태그\n총공 이벤트", null, 6));
        cheeringItemRepository.save(CheeringItem.of("VOTECOIN", "어쩌구\n저쩌구", null, 7));
    }
}
