package com.thevip.music.config;

import com.thevip.music.entity.MusicStreamingImage;
import com.thevip.music.repository.MusicStreamingImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 원클릭 스트리밍 화면 하단 "스트리밍 리스트" 이미지 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class MusicStreamingImageDataInitializer implements ApplicationRunner {

    private final MusicStreamingImageRepository musicStreamingImageRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (musicStreamingImageRepository.count() > 0) {
            return;
        }

        musicStreamingImageRepository.save(
                MusicStreamingImage.of("https://example.com/music/streaming/playlist-1.png", 0));
    }
}
