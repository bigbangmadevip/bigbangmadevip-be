package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thevip.music.dto.MusicDetailAdminRequest;
import com.thevip.music.dto.MusicDetailAdminResponse;
import com.thevip.music.entity.MusicCategory;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.music.service.MusicDetailAdminService;
import com.thevip.push.PushTopic;
import com.thevip.push.service.PushNotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MusicDetailAdminServiceTest {

    @Test
    void 긴급배너를_켜면_기존에_켜져있던_다른_상세는_꺼진다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicDetail existingUrgent = MusicDetail.of(MusicCategory.STREAMING, "기존 긴급", null, null, null);
        existingUrgent.updateMenuUrgent(true);
        ReflectionTestUtils.setField(existingUrgent, "id", 1L);
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(existingUrgent));

        MusicDetail newDetail = MusicDetail.of(MusicCategory.STREAMING, "새 긴급", null, null, null);
        ReflectionTestUtils.setField(newDetail, "id", 2L);
        when(musicDetailRepository.findById(2L)).thenReturn(Optional.of(newDetail));
        when(musicDetailRepository.findByMenuUrgentTrue()).thenReturn(List.of(existingUrgent));

        MusicDetailAdminService service =
                new MusicDetailAdminService(musicDetailRepository, mock(PushNotificationService.class));
        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "새 긴급", null, null, null, null, null, null, null,
                true, "새 긴급 배너", true, null, false, null, null, null);
        service.update(2L, request);

        assertThat(existingUrgent.isMenuUrgent()).isFalse();
        assertThat(newDetail.isMenuUrgent()).isTrue();
    }

    @Test
    void 즉시발송_설정이면_등록_시_푸시를_보내고_발송시각을_기록한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        MusicDetailAdminService service = new MusicDetailAdminService(musicDetailRepository, pushNotificationService);

        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "즉시발송 총공", null, null, null, null, null, null, null,
                false, null, true, null, true, null, "제목", "본문");

        MusicDetailAdminResponse response = service.create(request);

        verify(pushNotificationService).send(PushTopic.MUSIC, false, "제목", "본문");
        assertThat(response.pushSentAt()).isNotNull();
    }

    @Test
    void 예약발송_시각이_남아있으면_등록_시점에는_푸시를_보내지_않는다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        MusicDetailAdminService service = new MusicDetailAdminService(musicDetailRepository, pushNotificationService);

        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "예약발송 총공", null, null, null, null, null, null, null,
                false, null, true, null, true, LocalDateTime.now().plusHours(1), "제목", "본문");

        MusicDetailAdminResponse response = service.create(request);

        verifyNoInteractions(pushNotificationService);
        assertThat(response.pushSentAt()).isNull();
    }

    @Test
    void 이미_발송된_상세라도_다시_저장하면_또_발송한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        MusicDetailAdminService service = new MusicDetailAdminService(musicDetailRepository, pushNotificationService);

        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "즉시발송 총공", null, null, null, null, null, null, null,
                false, null, true, null, true, null, "제목", "본문");
        MusicDetail detail = MusicDetail.of(MusicCategory.STREAMING, "즉시발송 총공", null, null, null);
        ReflectionTestUtils.setField(detail, "id", 1L);
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        service.update(1L, request);
        MusicDetailAdminResponse response = service.update(1L, request);

        verify(pushNotificationService, times(2)).send(PushTopic.MUSIC, false, "제목", "본문");
        assertThat(response.pushSentAt()).isNotNull();
    }

    @Test
    void 예약시각을_다시_저장하면_발송기록이_초기화돼_스케줄러가_다시_잡을_수_있다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        MusicDetailAdminService service = new MusicDetailAdminService(musicDetailRepository, pushNotificationService);

        MusicDetail detail = MusicDetail.of(MusicCategory.STREAMING, "예약발송 총공", null, null, null);
        detail.updatePushEnabled(true);
        detail.markPushSent();
        ReflectionTestUtils.setField(detail, "id", 1L);
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "예약발송 총공", null, null, null, null, null, null, null,
                false, null, true, null, true, LocalDateTime.now().plusHours(1), "제목", "본문");
        MusicDetailAdminResponse response = service.update(1L, request);

        verifyNoInteractions(pushNotificationService);
        assertThat(response.pushSentAt()).isNull();
    }
}
