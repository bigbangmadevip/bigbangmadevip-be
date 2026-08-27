package com.thevip.music.config;

import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 원클릭 스트리밍(플랫폼 -> 운영체제 -> 링크) 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class MusicStreamingLinkDataInitializer implements ApplicationRunner {

    private final MusicStreamingLinkRepository musicStreamingLinkRepository;
    private final PlatformRepository platformRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (musicStreamingLinkRepository.count() > 0) {
            return;
        }

        Platform melon = platformRepository.findByName("멜론")
                .orElseGet(() -> platformRepository.save(
                        Platform.of("멜론", "melon", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null)));
        Platform genie = platformRepository.findByName("지니")
                .orElseGet(() -> platformRepository.save(
                        Platform.of("지니", "genie", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null)));

        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                melon.getId(), OperatingSystem.ANDROID, "멜론 앱으로 스트리밍", "melonapp://streaming", 0));
        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                melon.getId(), OperatingSystem.ANDROID, "멜론 웹으로 스트리밍", "https://www.melon.com/", 1));
        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                melon.getId(), OperatingSystem.IPHONE, "멜론 앱으로 스트리밍", "melonapp://streaming", 2));
        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                melon.getId(), OperatingSystem.IPAD, "멜론 앱으로 스트리밍", "melonapp://streaming", 3));

        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                genie.getId(), OperatingSystem.ANDROID, "지니 앱으로 스트리밍", "geniemusic://streaming", 0));
        musicStreamingLinkRepository.save(MusicStreamingLink.of(
                genie.getId(), OperatingSystem.IPHONE, "지니 앱으로 스트리밍", "geniemusic://streaming", 1));
    }
}
