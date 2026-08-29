package com.thevip.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thevip.push.service.PushNotificationService;
import com.thevip.vote.dto.VoteDetailAdminRequest;
import com.thevip.vote.dto.VoteDetailAdminResponse;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailAdminService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VoteDetailAdminServiceTest {

    @Test
    void 긴급배너를_켜면_기존에_켜져있던_다른_상세는_꺼진다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        VoteDetail existingUrgent = VoteDetail.of(VoteCategory.MUSIC_SHOW, "기존 긴급", null, null, null);
        existingUrgent.updateMenuUrgent(true);
        ReflectionTestUtils.setField(existingUrgent, "id", 1L);
        when(voteDetailRepository.findById(1L)).thenReturn(Optional.of(existingUrgent));

        VoteDetail newDetail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "새 긴급", null, null, null);
        ReflectionTestUtils.setField(newDetail, "id", 2L);
        when(voteDetailRepository.findById(2L)).thenReturn(Optional.of(newDetail));
        when(voteDetailRepository.findByMenuUrgentTrue()).thenReturn(List.of(existingUrgent));

        VoteDetailAdminService service =
                new VoteDetailAdminService(voteDetailRepository, mock(PushNotificationService.class));
        VoteDetailAdminRequest request = new VoteDetailAdminRequest(
                VoteCategory.MUSIC_SHOW, // category
                "새 긴급", // title
                null, // musicShowId
                null, // rewardDescription
                null, // platformIds
                null, // platformUrl
                null, // eventStartAt
                null, // eventEndAt
                null, // checklist
                null, // imageUrls
                null, // guideIds
                null, // ctaButtonLabel
                true, // menuUrgent
                "새 긴급 배너", // urgentContent
                true, // active
                null, // scheduledAt
                false, // pushEnabled
                null, // pushSendAt
                null, // pushTitle
                null); // pushBody
        service.update(2L, request);

        assertThat(existingUrgent.isMenuUrgent()).isFalse();
        assertThat(newDetail.isMenuUrgent()).isTrue();
    }

    @Test
    void 즉시발송_설정이면_등록_시_푸시를_보내고_발송시각을_기록한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        VoteDetailAdminService service =
                new VoteDetailAdminService(voteDetailRepository, pushNotificationService);

        VoteDetailAdminRequest request = new VoteDetailAdminRequest(
                VoteCategory.MUSIC_SHOW, "즉시발송 투표", null, null, null, null, null, null,
                null, null, null, null, false, null, true, null,
                true, null, "제목", "본문");

        VoteDetailAdminResponse response = service.create(request);

        verify(pushNotificationService).sendToAllUsers("제목", "본문");
        assertThat(response.pushSentAt()).isNotNull();
    }

    @Test
    void 예약발송_시각이_남아있으면_등록_시점에는_푸시를_보내지_않는다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PushNotificationService pushNotificationService = mock(PushNotificationService.class);
        VoteDetailAdminService service =
                new VoteDetailAdminService(voteDetailRepository, pushNotificationService);

        VoteDetailAdminRequest request = new VoteDetailAdminRequest(
                VoteCategory.MUSIC_SHOW, "예약발송 투표", null, null, null, null, null, null,
                null, null, null, null, false, null, true, null,
                true, LocalDateTime.now().plusHours(1), "제목", "본문");

        VoteDetailAdminResponse response = service.create(request);

        verifyNoInteractions(pushNotificationService);
        assertThat(response.pushSentAt()).isNull();
    }
}
