package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.home.dto.HomeScheduleItemResponse;
import com.thevip.home.dto.MenuType;
import com.thevip.home.service.HomeTodayScheduleService;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * @Cacheable이 붙은 무인자 메서드라 실제 스프링 컨텍스트/DB로 테스트하면 캐시가 공유되어
 * 여러 시나리오가 서로 오염된다. 캐시 프록시를 안 타도록 서비스를 직접 new해서 순수 로직만 검증한다.
 */
class HomeTodayScheduleServiceTest {

    private static final Platform MELON = Platform.of("멜론", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null);
    private static final Platform BUGS = Platform.of("벅스(Bugs)", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null);

    @Test
    void 음원만_노출이면_음원_하나짜리_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail detail = MusicDetail.of(CheeringCategory.DOWNLOAD, "테스트 총공", null,
                LocalDateTime.now(), 0);
        detail.addPlatformId(1L);
        detail.updateUrgentContent("긴급 배너 문구");
        when(musicDetailRepository.findTodayExposed(any(), any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any())).thenReturn(List.of());
        when(platformRepository.findById(eq(1L))).thenReturn(Optional.of(MELON));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get(0).title()).isEqualTo("긴급 배너 문구");
        assertThat(result.get(0).platformNames()).containsExactly("멜론");
    }

    @Test
    void 플랫폼이_여러개면_전부_리스트로_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail detail = MusicDetail.of(CheeringCategory.DOWNLOAD, "멜론, 벅스 flac 16bit 다운", null,
                LocalDateTime.now(), 0);
        detail.addPlatformId(1L);
        detail.addPlatformId(2L);
        detail.updateUrgentContent("멜론, 벅스 flac 16bit 다운");
        when(musicDetailRepository.findTodayExposed(any(), any(), any())).thenReturn(List.of(detail));
        when(voteDetailRepository.findTodayExposed(any())).thenReturn(List.of());
        when(platformRepository.findById(eq(1L))).thenReturn(Optional.of(MELON));
        when(platformRepository.findById(eq(2L))).thenReturn(Optional.of(BUGS));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).platformNames()).containsExactly("멜론", "벅스(Bugs)");
    }

    @Test
    void 음원_투표_둘다_있으면_시간순으로_정렬된_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail music = MusicDetail.of(CheeringCategory.DOWNLOAD, "음원", null,
                LocalDateTime.of(2026, 8, 8, 20, 30), 0);
        music.addPlatformId(1L);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                null, LocalDateTime.of(2026, 8, 8, 23, 59), 0);
        vote.addPlatformId(1L);
        when(musicDetailRepository.findTodayExposed(any(), any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findTodayExposed(any())).thenReturn(List.of(vote));
        when(platformRepository.findById(eq(1L))).thenReturn(Optional.of(MELON));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository);
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
        when(musicDetailRepository.findTodayExposed(any(), any(), any())).thenReturn(List.of());
        when(voteDetailRepository.findTodayExposed(any())).thenReturn(List.of());

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository);

        assertThat(service.getTodaySchedule()).isEmpty();
    }

    @Test
    void 노출_대상이_5개를_초과하면_시간순으로_5개까지만_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        LocalDateTime base = LocalDateTime.of(2026, 8, 8, 0, 0);
        List<MusicDetail> details = IntStream.range(0, 6)
                .mapToObj(i -> {
                    MusicDetail detail = MusicDetail.of(CheeringCategory.DOWNLOAD, "총공" + i, null,
                            base.plusHours(i), 0);
                    detail.addPlatformId(1L);
                    return detail;
                })
                .toList();
        when(musicDetailRepository.findTodayExposed(any(), any(), any())).thenReturn(details);
        when(voteDetailRepository.findTodayExposed(any())).thenReturn(List.of());
        when(platformRepository.findById(eq(1L))).thenReturn(Optional.of(MELON));

        HomeTodayScheduleService service = new HomeTodayScheduleService(
                musicDetailRepository, voteDetailRepository, platformRepository);
        List<HomeScheduleItemResponse> result = service.getTodaySchedule();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).time()).isEqualTo(base);
        assertThat(result.get(4).time()).isEqualTo(base.plusHours(4));
    }
}
