package com.thevip.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.dto.VoteTodayResponse;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteTodayService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class VoteTodayServiceTest {

    @Test
    void 긴급_투표가_있으면_urgent를_반환한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "테스트 투표", null, null, null, 0);
        detail.updateUrgentContent("긴급 투표 문구");
        when(voteDetailRepository.findVisibleMenuUrgent(any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findActiveOngoing(any())).thenReturn(List.of());

        VoteTodayService service = new VoteTodayService(voteDetailRepository, platformRepository);
        VoteTodayResponse result = service.getToday();

        assertThat(result.urgent()).isNotNull();
        assertThat(result.urgent().urgentContent()).isEqualTo("긴급 투표 문구");
    }

    @Test
    void 긴급_투표가_없으면_urgent는_null이다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(voteDetailRepository.findVisibleMenuUrgent(any())).thenReturn(List.of());
        when(voteDetailRepository.findActiveOngoing(any())).thenReturn(List.of());

        VoteTodayService service = new VoteTodayService(voteDetailRepository, platformRepository);

        assertThat(service.getToday().urgent()).isNull();
    }

    @Test
    void 진행중인_투표_목록을_마감_임박순으로_반환한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(voteDetailRepository.findVisibleMenuUrgent(any())).thenReturn(List.of());

        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "쇼음악중심 사전투표", "1위 트로피",
                LocalDateTime.of(2026, 8, 19, 11, 0), LocalDateTime.of(2026, 8, 21, 23, 0), 0);
        vote.addPlatformId(1L);
        vote.addImageUrl("https://example.com/vote/1.png");
        when(voteDetailRepository.findActiveOngoing(any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("뮤빗"));

        VoteTodayService service = new VoteTodayService(voteDetailRepository, platformRepository);
        VoteTodayResponse result = service.getToday();

        assertThat(result.votes()).hasSize(1);
        assertThat(result.votes().get(0).title()).isEqualTo("쇼음악중심 사전투표");
        assertThat(result.votes().get(0).platformNames()).containsExactly("뮤빗");
        assertThat(result.votes().get(0).imageUrl()).isEqualTo("https://example.com/vote/1.png");
        assertThat(result.votes().get(0).eventEndAt()).isEqualTo(LocalDateTime.of(2026, 8, 21, 23, 0));
    }
}
