package com.thevip.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.schedule.dto.ScheduleCategory;
import com.thevip.schedule.dto.ScheduleDayCountResponse;
import com.thevip.schedule.dto.ScheduleDayResponse;
import com.thevip.schedule.dto.ScheduleInitialResponse;
import com.thevip.schedule.dto.ScheduleMonthResponse;
import com.thevip.schedule.dto.VoteDisplayMode;
import com.thevip.schedule.service.ScheduleService;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

    @Test
    void 월간_조회는_음원과_투표_일자별_개수를_함께_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail music = MusicDetail.of(CheeringCategory.DOWNLOAD, "총공", null,
                LocalDateTime.of(2026, 8, 9, 19, 0), 0);
        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of(music));

        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                LocalDateTime.of(2026, 8, 9, 0, 0), LocalDateTime.of(2026, 8, 9, 23, 59), 0);
        when(voteDetailRepository.findActiveOverlapping(any(), any(), any())).thenReturn(List.of(vote));

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleMonthResponse result = service.getMonth(YearMonth.of(2026, 8), ScheduleCategory.ALL, VoteDisplayMode.EVERY_DAY);

        assertThat(result.days()).hasSize(1);
        ScheduleDayCountResponse day = result.days().get(0);
        assertThat(day.date()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(day.musicCount()).isEqualTo(1);
        assertThat(day.voteCount()).isEqualTo(1);
    }

    @Test
    void EVERY_DAY_모드면_투표는_시작일부터_마감일까지_매일_카운트된다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of());
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "3일짜리 투표", null,
                LocalDateTime.of(2026, 8, 5, 0, 0), LocalDateTime.of(2026, 8, 7, 23, 59), 0);
        when(voteDetailRepository.findActiveOverlapping(any(), any(), any())).thenReturn(List.of(vote));

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleMonthResponse result = service.getMonth(YearMonth.of(2026, 8), ScheduleCategory.ALL, VoteDisplayMode.EVERY_DAY);

        assertThat(result.days()).hasSize(3);
        assertThat(result.days()).extracting(ScheduleDayCountResponse::date)
                .containsExactly(LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 6), LocalDate.of(2026, 8, 7));
        assertThat(result.days()).allMatch(d -> d.voteCount() == 1 && d.musicCount() == 0);
    }

    @Test
    void DEADLINE_ONLY_모드면_투표는_마감일_하루만_카운트된다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of());
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "3일짜리 투표", null,
                LocalDateTime.of(2026, 8, 5, 0, 0), LocalDateTime.of(2026, 8, 7, 23, 59), 0);
        when(voteDetailRepository.findActiveByDeadlineInRange(any(), any(), any())).thenReturn(List.of(vote));

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleMonthResponse result = service.getMonth(YearMonth.of(2026, 8), ScheduleCategory.ALL, VoteDisplayMode.DEADLINE_ONLY);

        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 7));
        assertThat(result.days().get(0).voteCount()).isEqualTo(1);
    }

    @Test
    void MUSIC_필터면_투표는_카운트에서_제외된다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail music = MusicDetail.of(CheeringCategory.DOWNLOAD, "총공", null,
                LocalDateTime.of(2026, 8, 9, 19, 0), 0);
        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of(music));

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleMonthResponse result = service.getMonth(YearMonth.of(2026, 8), ScheduleCategory.MUSIC, VoteDisplayMode.EVERY_DAY);

        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).voteCount()).isZero();
    }

    @Test
    void 일별_조회는_시간순으로_정렬해서_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail late = MusicDetail.of(CheeringCategory.DOWNLOAD, "늦은 총공", null,
                LocalDateTime.of(2026, 8, 9, 20, 0), 0);
        late.updateUrgentContent("벅스 다운로드 총공");
        MusicDetail early = MusicDetail.of(CheeringCategory.DOWNLOAD, "이른 총공", null,
                LocalDateTime.of(2026, 8, 9, 19, 0), 0);
        early.updateUrgentContent("멜론 다운로드 총공");
        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of(late, early));
        when(voteDetailRepository.findActiveOverlapping(any(), any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleDayResponse result = service.getDay(LocalDate.of(2026, 8, 9), ScheduleCategory.ALL, VoteDisplayMode.EVERY_DAY);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).title()).isEqualTo("멜론 다운로드 총공");
        assertThat(result.items().get(1).title()).isEqualTo("벅스 다운로드 총공");
    }

    @Test
    void DEADLINE_ONLY_모드면_일별_조회도_마감일_기준_쿼리를_쓴다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of());
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null,
                LocalDateTime.of(2026, 8, 5, 0, 0), LocalDateTime.of(2026, 8, 7, 23, 59), 0);
        when(voteDetailRepository.findActiveByDeadlineInRange(any(), any(), any())).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleDayResponse result = service.getDay(LocalDate.of(2026, 8, 7), ScheduleCategory.ALL, VoteDisplayMode.DEADLINE_ONLY);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).title()).isEqualTo("투표");
    }

    @Test
    void 초기_조회는_월_카운트와_선택_날짜_리스트를_함께_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail music = MusicDetail.of(CheeringCategory.DOWNLOAD, "총공", null,
                LocalDateTime.of(2026, 8, 9, 19, 0), 0);
        when(musicDetailRepository.findActiveInRange(any(), any(), any())).thenReturn(List.of(music));
        when(voteDetailRepository.findActiveOverlapping(any(), any(), any())).thenReturn(List.of());
        when(platformRepository.findNamesByIds(any())).thenReturn(List.of());

        ScheduleService service = new ScheduleService(musicDetailRepository, voteDetailRepository, platformRepository);
        ScheduleInitialResponse result = service.getInitial(YearMonth.of(2026, 8), LocalDate.of(2026, 8, 9),
                ScheduleCategory.ALL, VoteDisplayMode.EVERY_DAY);

        assertThat(result.month().days()).hasSize(1);
        assertThat(result.month().days().get(0).date()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(result.day().date()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(result.day().items()).hasSize(1);
    }
}
