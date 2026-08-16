package com.thevip.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.global.exception.BusinessException;
import com.thevip.guide.entity.Guide;
import com.thevip.guide.entity.GuideType;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.dto.VoteDetailResponse;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import com.thevip.vote.service.VoteDetailService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VoteDetailServiceTest {

    @Test
    void 상세를_조회하면_플랫폼명과_가이드를_같이_반환한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "테스트 투표", "1위 리워드",
                LocalDateTime.of(2026, 8, 19, 0, 0), LocalDateTime.of(2026, 8, 21, 23, 59), 0);
        detail.addPlatformId(1L);
        detail.addChecklistItem("체크1");
        detail.addGuideId(10L);
        detail.updatePlatformUrl("https://example.com/vote");
        detail.updateCtaButtonLabel("투표하러 가기");
        when(voteDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("뮤빗"));
        Guide guide = Guide.of(GuideType.STREAMING, 1L, "뮤빗 투표 방법", 0);
        when(guideRepository.findActiveByIds(List.of(10L))).thenReturn(List.of(guide));

        VoteDetailService service = new VoteDetailService(voteDetailRepository, platformRepository, guideRepository);
        VoteDetailResponse result = service.getDetail(1L);

        assertThat(result.title()).isEqualTo("테스트 투표");
        assertThat(result.platformNames()).containsExactly("뮤빗");
        assertThat(result.checklist()).containsExactly("체크1");
        assertThat(result.platformUrl()).isEqualTo("https://example.com/vote");
        assertThat(result.ctaButtonLabel()).isEqualTo("투표하러 가기");
        assertThat(result.guides()).hasSize(1);
        assertThat(result.guides().get(0).title()).isEqualTo("뮤빗 투표 방법");
    }

    @Test
    void 존재하지_않으면_예외가_발생한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);
        when(voteDetailRepository.findById(999L)).thenReturn(Optional.empty());

        VoteDetailService service = new VoteDetailService(voteDetailRepository, platformRepository, guideRepository);

        assertThatThrownBy(() -> service.getDetail(999L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 예약시각이_지나지_않았으면_예외가_발생한다() {
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "테스트 투표", null, null, null, 0);
        detail.updateScheduledAt(LocalDateTime.now().plusDays(1));
        when(voteDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        VoteDetailService service = new VoteDetailService(voteDetailRepository, platformRepository, guideRepository);

        assertThatThrownBy(() -> service.getDetail(1L)).isInstanceOf(BusinessException.class);
    }
}
