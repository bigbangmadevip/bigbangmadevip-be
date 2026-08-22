package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.entity.MusicCategory;
import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.home.dto.MenuType;
import com.thevip.home.service.HomeTodayScheduleService;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailPlatformResolver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class HomeTodayScheduleServiceTest {

    @Test
    void 음원만_노출이면_음원_하나짜리_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", null,
                LocalDateTime.now(), null, 0);
        detail.addPlatformId(1L);
        detail.updateUrgentContent("긴급 배너 문구");
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get(0).title()).isEqualTo("테스트 총공");
        assertThat(result.get(0).platformNames()).containsExactly("멜론");
    }

    @Test
    void 플랫폼이_여러개면_전부_리스트로_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "멜론, 벅스 flac 16bit 다운", null,
                LocalDateTime.now(), null, 0);
        detail.addPlatformId(1L);
        detail.addPlatformId(2L);
        detail.updateUrgentContent("멜론, 벅스 flac 16bit 다운");
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L, 2L))).thenReturn(List.of("멜론", "벅스(Bugs)"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).platformNames()).containsExactly("멜론", "벅스(Bugs)");
    }

    @Test
    void 음원_투표_둘다_있으면_시간순으로_정렬된_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "음원", null,
                LocalDateTime.of(2026, 8, 8, 20, 30), null, 0);
        music.addPlatformId(1L);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                null, LocalDateTime.of(2026, 8, 8, 23, 59), 0);
        vote.addPlatformId(1L);
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get(1).menuType()).isEqualTo(MenuType.VOTE);
    }

    @Test
    void 둘_다_없으면_빈_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver);

        assertThat(service.getTodaySchedule()).isEmpty();
    }

    @Test
    void 노출_대상이_5개를_초과하면_시간순으로_5개까지만_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        LocalDateTime base = LocalDateTime.of(2026, 8, 8, 0, 0);
        List<MusicDetail> details = IntStream.range(0, 6)
                .mapToObj(i -> {
                    MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "총공" + i, null,
                            base.plusHours(i), null, 0);
                    detail.addPlatformId(1L);
                    return detail;
                })
                .toList();
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(details);
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).time()).isEqualTo(base);
        assertThat(result.get(4).time()).isEqualTo(base.plusHours(4));
    }
}
