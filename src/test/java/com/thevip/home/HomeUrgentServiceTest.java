package com.thevip.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.home.dto.HomeUrgentResponse;
import com.thevip.home.dto.MenuType;
import com.thevip.home.service.HomeUrgentService;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @Cacheable이 붙은 무인자 메서드라 실제 스프링 컨텍스트/DB로 테스트하면 캐시가 공유되어
 * 여러 시나리오가 서로 오염된다. 캐시 프록시를 안 타도록 서비스를 직접 new해서 순수 로직만 검증한다.
 */
class HomeUrgentServiceTest {

    @Test
    void 음원_상세에_homeUrgent가_있으면_그것을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        MusicDetail detail = MusicDetail.of("다운로드", "테스트 총공", null, "멜론", null);
        when(musicDetailRepository.findByHomeUrgentTrueAndActiveTrue()).thenReturn(List.of(detail));

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository);
        Optional<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).isPresent();
        assertThat(result.get().menuType()).isEqualTo(MenuType.MUSIC);
        assertThat(result.get().category()).isEqualTo("다운로드");
        assertThat(result.get().title()).isEqualTo("테스트 총공");
    }

    @Test
    void 음원이_없고_투표만_있으면_투표를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        when(musicDetailRepository.findByHomeUrgentTrueAndActiveTrue()).thenReturn(List.of());
        VoteDetail detail = VoteDetail.of("테스트 투표", null, "하이어", null, null);
        when(voteDetailRepository.findByHomeUrgentTrueAndActiveTrue()).thenReturn(List.of(detail));

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository);
        Optional<HomeUrgentResponse> result = service.getCurrentUrgent();

        assertThat(result).isPresent();
        assertThat(result.get().menuType()).isEqualTo(MenuType.VOTE);
        assertThat(result.get().title()).isEqualTo("테스트 투표");
    }

    @Test
    void 둘_다_없으면_빈값을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        VoteDetailRepository voteDetailRepository = mock(VoteDetailRepository.class);
        when(musicDetailRepository.findByHomeUrgentTrueAndActiveTrue()).thenReturn(List.of());
        when(voteDetailRepository.findByHomeUrgentTrueAndActiveTrue()).thenReturn(List.of());

        HomeUrgentService service = new HomeUrgentService(musicDetailRepository, voteDetailRepository);

        assertThat(service.getCurrentUrgent()).isEmpty();
    }
}
