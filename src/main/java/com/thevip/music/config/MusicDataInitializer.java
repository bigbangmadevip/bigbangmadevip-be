package com.thevip.music.config;

import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.repository.PlatformRepository;
import java.time.LocalDate;
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
    private final PlatformRepository platformRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (musicDetailRepository.count() > 0) {
            return;
        }

        Platform melon = platformRepository.findByName("멜론")
                .orElseGet(() -> platformRepository.save(Platform.of("멜론", PlatformRegion.DOMESTIC, null)));
        Platform bugs = platformRepository.findByName("벅스(Bugs)")
                .orElseGet(() -> platformRepository.save(Platform.of("벅스(Bugs)", PlatformRegion.DOMESTIC, null)));

        MusicDetail detail = MusicDetail.of(
                CheeringCategory.DOWNLOAD,
                "오늘 저녁 8시 30분 멜론 개별곡 다운로드 총공",
                "타이틀 곡 <봄여름가을겨울>",
                LocalDate.now().atTime(20, 30),
                0);
        detail.addPlatformId(melon.getId());
        detail.addPlatformId(bugs.getId());
        detail.addChecklistItem("Too Bad, Home sweet Home, Live Fast Die Slow 스트리밍 필수");
        detail.addChecklistItem("다운로드 파일 삭제 확인 후 진행");
        detail.updateMenuUrgent(true);
        detail.updateTodayExposed(true);
        detail.updateUrgentContent("오늘 저녁 8시 30분 멜론 다운로드 총공");
        musicDetailRepository.save(detail);
    }
}
