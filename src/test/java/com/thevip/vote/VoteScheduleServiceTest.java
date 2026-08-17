package com.thevip.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.global.exception.BusinessException;
import com.thevip.guide.entity.Guide;
import com.thevip.guide.entity.GuideType;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.vote.dto.VoteScheduleDetailResponse;
import com.thevip.vote.dto.VoteScheduleListItemResponse;
import com.thevip.vote.entity.MusicShow;
import com.thevip.vote.entity.MusicShowVoteRound;
import com.thevip.vote.repository.MusicShowRepository;
import com.thevip.vote.repository.MusicShowVoteRoundRepository;
import com.thevip.vote.service.VoteScheduleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VoteScheduleServiceTest {

    @Test
    void 목록을_조회하면_활성화된_방송만_정렬해서_반환한다() {
        MusicShowRepository musicShowRepository = mock(MusicShowRepository.class);
        MusicShowVoteRoundRepository musicShowVoteRoundRepository = mock(MusicShowVoteRoundRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        MusicShow show = MusicShow.of("쇼! 음악중심", 0);
        show.updateIconUrl("https://example.com/icon.png");
        show.updateBroadcastTime("매주 토요일 오후 3:55");
        when(musicShowRepository.findByActiveTrueOrderBySortOrder()).thenReturn(List.of(show));

        VoteScheduleService service =
                new VoteScheduleService(musicShowRepository, musicShowVoteRoundRepository, guideRepository);
        List<VoteScheduleListItemResponse> result = service.getSchedules();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("쇼! 음악중심");
        assertThat(result.get(0).broadcastTime()).isEqualTo("매주 토요일 오후 3:55");
    }

    @Test
    void 상세를_조회하면_라운드와_가이드를_같이_반환한다() {
        MusicShowRepository musicShowRepository = mock(MusicShowRepository.class);
        MusicShowVoteRoundRepository musicShowVoteRoundRepository = mock(MusicShowVoteRoundRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        MusicShow show = MusicShow.of("쇼! 음악중심", 0);
        show.updateChannel("MBC");
        show.addGuideId(10L);
        when(musicShowRepository.findById(1L)).thenReturn(Optional.of(show));

        MusicShowVoteRound round = MusicShowVoteRound.of(1L, "사전 투표 1", "8/12(화) 10:00", "advance", 0);
        round.addRow("뮤빗", "바로가기");
        when(musicShowVoteRoundRepository.findByMusicShowIdAndActiveTrueOrderBySortOrder(1L))
                .thenReturn(List.of(round));

        Guide guide = Guide.of(GuideType.STREAMING, 1L, "뮤빗 투표 방법", 0);
        when(guideRepository.findActiveByIds(List.of(10L))).thenReturn(List.of(guide));

        VoteScheduleService service =
                new VoteScheduleService(musicShowRepository, musicShowVoteRoundRepository, guideRepository);
        VoteScheduleDetailResponse result = service.getSchedule(1L);

        assertThat(result.channel()).isEqualTo("MBC");
        assertThat(result.rounds()).hasSize(1);
        assertThat(result.rounds().get(0).label()).isEqualTo("사전 투표 1");
        assertThat(result.rounds().get(0).rows()).hasSize(1);
        assertThat(result.rounds().get(0).rows().get(0).label()).isEqualTo("뮤빗");
        assertThat(result.guides()).hasSize(1);
        assertThat(result.guides().get(0).title()).isEqualTo("뮤빗 투표 방법");
    }

    @Test
    void 존재하지_않으면_예외가_발생한다() {
        MusicShowRepository musicShowRepository = mock(MusicShowRepository.class);
        MusicShowVoteRoundRepository musicShowVoteRoundRepository = mock(MusicShowVoteRoundRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);
        when(musicShowRepository.findById(999L)).thenReturn(Optional.empty());

        VoteScheduleService service =
                new VoteScheduleService(musicShowRepository, musicShowVoteRoundRepository, guideRepository);

        assertThatThrownBy(() -> service.getSchedule(999L)).isInstanceOf(BusinessException.class);
    }
}
