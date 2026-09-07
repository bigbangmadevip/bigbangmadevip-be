package com.thevip.youtube.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// 홈 화면에 노출할 유튜브 영상 조회수를 1시간마다 갱신해 메모리에 캐시해둔다. 매 홈 요청마다
// YouTube API를 호출하면 무료 할당량(10,000 units/day)을 금방 소진하므로, 스케줄러가 미리
// 가져온 값을 그대로 내려준다. API 키/영상 ID가 설정되지 않으면 조용히 갱신을 건너뛴다
// (firebase.service-account-key-base64와 동일한 패턴 - PushNotificationService 참고).
@Service
@Slf4j
public class YoutubeViewCountService {

    private static final String VIDEOS_URI = "https://www.googleapis.com/youtube/v3/videos?part=statistics&id={videoId}&key={apiKey}";

    @Value("${youtube.api-key:}")
    private String apiKey;

    @Value("${youtube.video-id:}")
    private String videoId;

    private final RestClient restClient = RestClient.create();

    private volatile Long viewCount;

    public Long getViewCount() {
        return viewCount;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        if (apiKey.isBlank() || videoId.isBlank()) {
            log.warn("YouTube API 키/영상 ID가 설정되지 않아 조회수 갱신을 건너뜁니다.");
            return;
        }
        try {
            VideosResponse response = restClient.get()
                    .uri(VIDEOS_URI, videoId, apiKey)
                    .retrieve()
                    .body(VideosResponse.class);
            if (response == null || response.items() == null || response.items().isEmpty()) {
                log.warn("YouTube 응답에 영상 정보가 없습니다. videoId={}", videoId);
                return;
            }
            viewCount = Long.parseLong(response.items().get(0).statistics().viewCount());
        } catch (RestClientException | NumberFormatException e) {
            log.error("YouTube 조회수 갱신 실패", e);
        }
    }

    private record VideosResponse(List<Item> items) {
        private record Item(Statistics statistics) {
        }

        private record Statistics(String viewCount) {
        }
    }
}
