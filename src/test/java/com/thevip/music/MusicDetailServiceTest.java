package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.entity.MusicCategory;
import com.thevip.global.exception.BusinessException;
import com.thevip.guide.entity.Guide;
import com.thevip.guide.entity.GuideType;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.music.dto.MusicDetailResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.music.service.MusicDetailService;
import com.thevip.platform.repository.PlatformRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MusicDetailServiceTest {

    @Test
    void 상세를_조회하면_플랫폼명과_가이드를_같이_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", "테스트 곡",
                LocalDateTime.of(2026, 8, 9, 20, 30), 0);
        detail.addPlatformId(1L);
        detail.addChecklistItem("체크1");
        detail.addGuideId(10L);
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));
        Guide guide = Guide.of(GuideType.DOWNLOAD, 1L, "멜론 다운로드 방법", 0);
        when(guideRepository.findActiveByIds(List.of(10L))).thenReturn(List.of(guide));

        MusicDetailService service = new MusicDetailService(musicDetailRepository, platformRepository, guideRepository);
        MusicDetailResponse result = service.getDetail(1L);

        assertThat(result.title()).isEqualTo("테스트 총공");
        assertThat(result.platformNames()).containsExactly("멜론");
        assertThat(result.checklist()).containsExactly("체크1");
        assertThat(result.guides()).hasSize(1);
        assertThat(result.guides().get(0).title()).isEqualTo("멜론 다운로드 방법");
    }

    @Test
    void 존재하지_않으면_예외가_발생한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);
        when(musicDetailRepository.findById(999L)).thenReturn(Optional.empty());

        MusicDetailService service = new MusicDetailService(musicDetailRepository, platformRepository, guideRepository);

        assertThatThrownBy(() -> service.getDetail(999L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 예약시각이_지나지_않았으면_예외가_발생한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        GuideRepository guideRepository = mock(GuideRepository.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", null, null, 0);
        detail.updateScheduledAt(LocalDateTime.now().plusDays(1));
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        MusicDetailService service = new MusicDetailService(musicDetailRepository, platformRepository, guideRepository);

        assertThatThrownBy(() -> service.getDetail(1L)).isInstanceOf(BusinessException.class);
    }
}
