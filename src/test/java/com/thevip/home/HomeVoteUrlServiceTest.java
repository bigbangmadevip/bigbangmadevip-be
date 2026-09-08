package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.home.dto.HomeVoteUrlResponse;
import com.thevip.home.service.HomeVoteUrlService;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class HomeVoteUrlServiceTest {

    @Test
    void 진행중인_음악방송_투표의_제목_플랫폼_URL을_반환한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "인기가요 생방송 투표", null,
                null, LocalDateTime.of(2026, 8, 10, 23, 59));
        detail.replacePlatformUrl(List.of("https://vote.example.com/higher"));
        when(voteDetailRepository.findActiveOngoingByCategory(
                org.mockito.ArgumentMatchers.eq(VoteCategory.MUSIC_SHOW), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(detail));
        when(voteDetailPlatformResolver.resolveNames(detail)).thenReturn(List.of("하이어(Higher)"));

        HomeVoteUrlService service = new HomeVoteUrlService(voteDetailRepository, voteDetailPlatformResolver);
        List<HomeVoteUrlResponse> result = service.getOngoingMusicShowVoteUrls();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("인기가요 생방송 투표");
        assertThat(result.get(0).platformNames()).containsExactly("하이어(Higher)");
        assertThat(result.get(0).platformUrl()).containsExactly("https://vote.example.com/higher");
    }

    @Test
    void 진행중인_음악방송_투표가_없으면_빈_리스트를_반환한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        when(voteDetailRepository.findActiveOngoingByCategory(
                org.mockito.ArgumentMatchers.eq(VoteCategory.MUSIC_SHOW), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        HomeVoteUrlService service = new HomeVoteUrlService(voteDetailRepository, voteDetailPlatformResolver);

        assertThat(service.getOngoingMusicShowVoteUrls()).isEmpty();
    }
}
