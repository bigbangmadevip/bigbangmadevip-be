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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class HomeTodayScheduleServiceTest {

    // 시:분만 비교하는 정렬/필터가 실제 시각(자정 근처 등)에 흔들리지 않도록 낮 12시로 고정해둔다.
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 8, 12, 0);
    private static final Clock CLOCK = Clock.fixed(NOW.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Test
    void 음원만_노출이면_음원_하나짜리_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", null,
                NOW.plusMinutes(1), NOW.plusHours(1));
        detail.addPlatformId(1L);
        detail.updateUrgentContent("긴급 배너 문구");
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
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
                NOW.plusMinutes(1), NOW.plusHours(1));
        detail.addPlatformId(1L);
        detail.addPlatformId(2L);
        detail.updateUrgentContent("멜론, 벅스 flac 16bit 다운");
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L, 2L))).thenReturn(List.of("멜론", "벅스(Bugs)"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).platformNames()).containsExactly("멜론", "벅스(Bugs)");
    }

    @Test
    void 음원_투표_둘다_있으면_시작시간순으로_정렬된_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        // 지난 시작시간은 필터로 빠지므로, 시간대는 항상 NOW 기준 미래인 상대값으로 둔다.
        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "음원", null,
                NOW.plusHours(4), NOW.plusHours(5));
        music.addPlatformId(1L);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                NOW.plusHours(2), NOW.plusHours(6));
        vote.addPlatformId(1L);
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.VOTE);
        assertThat(result.get(1).menuType()).isEqualTo(MenuType.MUSIC);
    }

    @Test
    void 시작시간이_없는_투표는_이미_시작된_것으로_보고_맨_앞에_온다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "음원", null,
                NOW.plusHours(1), NOW.plusHours(2));
        music.addPlatformId(1L);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                null, NOW.plusHours(3));
        vote.addPlatformId(1L);
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.VOTE);
        assertThat(result.get(1).menuType()).isEqualTo(MenuType.MUSIC);
    }

    @Test
    void 시작일이_며칠_전이어도_시작_시간대로만_정렬된다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        // 음원은 3일 전에 시작해서 아직 진행 중, 투표는 오늘 시작. 날짜는 무시하고 시:분만
        // 비교하므로 시작일과 무관하게 시간이 더 이른 투표가 음원보다 먼저 나와야 한다.
        MusicDetail music = MusicDetail.of(MusicCategory.DOWNLOAD, "며칠째 진행 중인 총공", null,
                NOW.plusHours(3).minusDays(3), NOW.plusDays(20));
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "오늘 시작한 투표", null,
                NOW.plusHours(1), NOW.plusHours(10));
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());
        when(voteDetailPlatformResolver.resolveNames(vote)).thenReturn(List.of());

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).extracting(HomeScheduleItemResponse::menuType)
                .containsExactly(MenuType.VOTE, MenuType.MUSIC);
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
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);

        assertThat(service.getTodaySchedule()).isEmpty();
    }

    @Test
    void 노출_대상이_5개를_초과하면_시작시간순으로_5개까지만_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        VoteDetailPlatformResolver voteDetailPlatformResolver = mock(VoteDetailPlatformResolver.class);

        LocalDateTime base = NOW.plusHours(1);
        List<MusicDetail> details = IntStream.range(0, 6)
                .mapToObj(i -> {
                    MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "총공" + i, null,
                            base.plusHours(i), base.plusHours(i).plusHours(1));
                    detail.addPlatformId(1L);
                    return detail;
                })
                .toList();
        when(musicDetailRepository.findTodayExposed(any(), any())).thenReturn(details);
        when(voteDetailRepository.findTodayExposed(any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository, voteDetailPlatformResolver, CLOCK);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).time()).isEqualTo(base);
        assertThat(result.get(4).time()).isEqualTo(base.plusHours(4));
    }
}
