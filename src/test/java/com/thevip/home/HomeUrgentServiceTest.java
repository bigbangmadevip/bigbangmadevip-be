package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.cheering.entity.CheeringCategory;
import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.home.dto.MenuType;
import com.thevip.home.service.HomeUrgentService;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.vote.entity.VoteCategory;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @Cacheable이 붙은 무인자 메서드라 실제 스프링 컨텍스트/DB로 테스트하면 캐시가 공유되어
 * 여러 시나리오가 서로 오염된다. 캐시 프록시를 안 타도록 서비스를 직접 new해서 순수 로직만 검증한다.
 */
class HomeUrgentServiceTest {

    @Test
    void 음원만_긴급이면_음원을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        MusicDetail detail = MusicDetail.of(CheeringCategory.DOWNLOAD, "테스트 총공", null, null, 0);
        detail.addPlatformId(1L);
        detail.updateUrgentContent("긴급 배너 문구");
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(detail));
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(platformRepository.findNamesByIds(List.of(1L))).thenReturn(List.of("멜론"));

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository);
        Optional<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).isPresent();
        assertThat(result.get().menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get().category()).isEqualTo("DOWNLOAD");
        assertThat(result.get().title()).isEqualTo("긴급 배너 문구");
        assertThat(result.get().platformNames()).containsExactly("멜론");
    }

    @Test
    void 음원이_없고_투표만_있으면_투표를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        VoteDetail detail = VoteDetail.of(VoteCategory.MUSIC_SHOW, "테스트 투표", null, null, null, 0);
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(detail));
        when(platformRepository.findNamesByIds(List.of())).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository);
        Optional<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).isPresent();
        assertThat(result.get().menuType()).isEqualTo(MenuType.VOTE);
        assertThat(result.get().title()).isEqualTo("테스트 투표");
    }

    @Test
    void 음원_투표_둘다_있으면_날짜가_더_임박한_쪽을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        MusicDetail music = MusicDetail.of(CheeringCategory.DOWNLOAD, "음원", null,
                LocalDateTime.of(2026, 8, 10, 0, 0), 0);
        VoteDetail vote = VoteDetail.of(VoteCategory.MUSIC_SHOW, "투표", null, null,
                LocalDateTime.of(2026, 8, 5, 0, 0), 0);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(music));
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(vote));
        when(platformRepository.findNamesByIds(List.of())).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository);
        Optional<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).isPresent();
        assertThat(result.get().menuType()).isEqualTo(MenuType.VOTE);
    }

    @Test
    void 둘_다_없으면_빈값을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(voteDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository,
                platformRepository);

        assertThat(service.getCurrentUrgent()).isEmpty();
    }
}
