package com.thevip.push.scheduler;

import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.push.PushTopic;
import com.thevip.push.service.PushNotificationService;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 예약발송(pushSendAt 지정) 대상을 1분마다 폴링해서 시각이 지난 건을 발송한다. 상태를 DB(pushSentAt)로
// 추적하기 때문에 배포로 컨테이너가 재시작돼도(매 main 푸시마다 재기동됨) 유실 없이 다음 폴링에서 이어진다.
@Component
@RequiredArgsConstructor
public class PushScheduler {

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;
    private final PushNotificationService pushNotificationService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void sendDuePush() {
        LocalDateTime now = LocalDateTime.now();
        for (MusicDetail detail : musicDetailRepository
                .findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(now)) {
            pushNotificationService.send(PushTopic.MUSIC, detail.isMenuUrgent(), detail.getPushTitle(), detail.getPushBody());
            detail.markPushSent();
        }
        for (VoteDetail detail : voteDetailRepository
                .findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(now)) {
            pushNotificationService.send(PushTopic.VOTE, detail.isMenuUrgent(), detail.getPushTitle(), detail.getPushBody());
            detail.markPushSent();
        }
    }
}
