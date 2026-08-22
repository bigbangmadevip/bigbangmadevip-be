package com.thevip.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.vote.dto.VoteDetailAdminRequest;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailAdminService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VoteDetailAdminServiceTest {

    @Test
    void 긴급배너를_켜면_기존에_켜져있던_다른_상세는_꺼진다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        VoteDetail existingUrgent = VoteDetail.of(VoteCategory.MUSIC_SHOW, "기존 긴급", null, null, null, 0);
        existingUrgent.updateMenuUrgent(true);
        ReflectionTestUtils.setField(existingUrgent, "id", 1L);
        when(voteDetailRepository.findById(1L)).thenReturn(Optional.of(existingUrgent));

        VoteDetail newDetail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "새 긴급", null, null, null, 1);
        ReflectionTestUtils.setField(newDetail, "id", 2L);
        when(voteDetailRepository.findById(2L)).thenReturn(Optional.of(newDetail));
        when(voteDetailRepository.findByMenuUrgentTrue()).thenReturn(List.of(existingUrgent));

        VoteDetailAdminService service = new VoteDetailAdminService(voteDetailRepository);
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
                null, // pushBody
                1); // sortOrder
        service.update(2L, request);

        assertThat(existingUrgent.isMenuUrgent()).isFalse();
        assertThat(newDetail.isMenuUrgent()).isTrue();
    }
}
