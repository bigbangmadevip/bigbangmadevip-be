package com.thevip.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thevip.music.entity.MusicCategory;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.push.scheduler.PushScheduler;
import com.thevip.push.service.PushNotificationService;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class PushSchedulerTest {

    @Test
    void 발송_시각이_지난_예약건을_보내고_발송시각을_기록한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);

        MusicDetail dueMusic = MusicDetail.of(MusicCategory.STREAMING, "예약 총공", null, null, null);
        dueMusic.updatePushEnabled(true);
        dueMusic.updatePushTitle("음원 제목");
        dueMusic.updatePushBody("음원 본문");
        when(musicDetailRepository.findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(any()))
                .thenReturn(List.of(dueMusic));

        VoteDetail dueVote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "예약 투표", null, null, null);
        dueVote.updatePushEnabled(true);
        dueVote.updatePushTitle("투표 제목");
        dueVote.updatePushBody("투표 본문");
        when(voteDetailRepository.findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(any()))
                .thenReturn(List.of(dueVote));

        PushScheduler scheduler = new PushScheduler(musicDetailRepository, voteDetailRepository, pushNotificationService);
        scheduler.sendDuePush();

        verify(pushNotificationService).sendToAllUsers("음원 제목", "음원 본문");
        verify(pushNotificationService).sendToAllUsers("투표 제목", "투표 본문");
        assertThat(dueMusic.getPushSentAt()).isNotNull();
        assertThat(dueVote.getPushSentAt()).isNotNull();
    }

    @Test
    void 대상이_없으면_아무것도_보내지_않는다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        when(musicDetailRepository.findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(any()))
                .thenReturn(List.of());
        when(voteDetailRepository.findByPushEnabledTrueAndPushSendAtLessThanEqualAndPushSentAtIsNull(any()))
                .thenReturn(List.of());

        PushScheduler scheduler = new PushScheduler(musicDetailRepository, voteDetailRepository, pushNotificationService);
        scheduler.sendDuePush();

        verifyNoInteractions(pushNotificationService);
    }
}
