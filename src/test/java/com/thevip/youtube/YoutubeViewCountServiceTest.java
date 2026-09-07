package com.thevip.youtube;

import static org.assertj.core.api.Assertions.assertThat;

import com.thevip.youtube.service.YoutubeViewCountService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class YoutubeViewCountServiceTest {

    @Test
    void API_키가_설정되지_않으면_예외없이_갱신을_건너뛴다() {
        YoutubeViewCountService service = new YoutubeViewCountService();
        ReflectionTestUtils.setField(service, "apiKey", "");
        ReflectionTestUtils.setField(service, "videoId", "");

        service.refresh();

        assertThat(service.getViewCount()).isNull();
    }

    @Test
    void 영상_ID만_설정되지_않으면_예외없이_갱신을_건너뛴다() {
        YoutubeViewCountService service = new YoutubeViewCountService();
        ReflectionTestUtils.setField(service, "apiKey", "dummy-key");
        ReflectionTestUtils.setField(service, "videoId", "");

        service.refresh();

        assertThat(service.getViewCount()).isNull();
    }
}
