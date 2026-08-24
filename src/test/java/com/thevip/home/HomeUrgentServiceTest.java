package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.entity.MusicCategory;
import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.home.dto.MenuType;
import com.thevip.home.service.HomeUrgentService;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class HomeUrgentServiceTest {

    @Test
    void 음원만_긴급이면_음원만_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", null, null, null);
        detail.addPlatformId(1L);
        detail.updateUrgentContent("긴급 배너 문구");
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(detail));
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository, voteDetailPlatformResolver);
        List<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get(0).category()).isEqualTo("DOWNLOAD");
        assertThat(result.get(0).title()).isEqualTo("긴급 배너 문구");
        assertThat(result.get(0).platformNames()).containsExactly("멜론");
    }

    @Test
    void 음원이_없고_투표만_있으면_투표만_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "테스트 투표", null, null, null);
        detail.updateUrgentContent("긴급 배너 문구");
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(detail));
        when(voteDetailPlatformResolver.resolveNames(detail)).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository, voteDetailPlatformResolver);
        List<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.VOTE);
        assertThat(result.get(0).title()).isEqualTo("긴급 배너 문구");
    }

    @Test
    void 음원_투표_둘다_있으면_마감_임박순으로_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "음원", null,
                LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 10, 23, 59));
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null, null,
                LocalDateTime.of(2026, 8, 5, 23, 59));
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(music));
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository, voteDetailPlatformResolver);
        List<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(HomeUrgentResponse::menuType)
                .containsExactly(MenuType.VOTE, MenuType.MUSIC);
    }

    @Test
    void 마감시간이_같으면_음원이_먼저_나온다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        LocalDateTime sameDeadline = LocalDateTime.of(2026, 8, 10, 23, 59);
        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "음원", null,
                LocalDateTime.of(2026, 8, 1, 0, 0), sameDeadline);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null, null, sameDeadline);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(music));
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository, voteDetailPlatformResolver);
        List<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).extracting(HomeUrgentResponse::menuType)
                .containsExactly(MenuType.MUSIC, MenuType.VOTE);
    }

    @Test
    void 둘_다_없으면_빈_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository, voteDetailPlatformResolver);

        assertThat(service.getCurrentUrgent()).isEmpty();
    }
}
