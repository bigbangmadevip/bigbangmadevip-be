package com.thevip.music.config;

import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 음원 상세 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class MusicDataInitializer implements ApplicationRunner {

    private final MusicDetailRepository musicDetailRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (musicDetailRepository.count() > 0) {
            return;
        }

        MusicDetail detail = MusicDetail.of(
                "다운로드",
                "오늘 저녁 8시 30분 멜론 개별곡 다운로드 총공",
                "타이틀 곡 <봄여름가을겨울>",
                "멜론",
                LocalDateTime.of(2026, 7, 14, 20, 30));
        detail.addChecklistItem("Too Bad, Home sweet Home, Live Fast Die Slow 스트리밍 필수");
        detail.addChecklistItem("다운로드 파일 삭제 확인 후 진행");
        detail.updateHomeUrgent(true);
        musicDetailRepository.save(detail);
    }
}
