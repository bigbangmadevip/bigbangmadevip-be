package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.dto.StreamingPlatformResponse;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.music.service.MusicStreamingService;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MusicStreamingServiceTest {

    @Test
    void 플랫폼별_운영체제별로_링크를_묶어서_반환한다() {
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicStreamingLink androidLink1 = MusicStreamingLink.of(1L, OperatingSystem.ANDROID, "앱으로 열기", "melon://a", 0);
        MusicStreamingLink androidLink2 = MusicStreamingLink.of(1L, OperatingSystem.ANDROID, "웹으로 열기", "https://a", 1);
        MusicStreamingLink iphoneLink = MusicStreamingLink.of(1L, OperatingSystem.IPHONE, "앱으로 열기", "melon://a", 2);
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc())
                .thenReturn(List.of(androidLink1, androidLink2, iphoneLink));

        Platform melon = Platform.of("멜론", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null);
        when(platformRepository.findById(1L)).thenReturn(Optional.of(melon));

        MusicStreamingService service = new MusicStreamingService(linkRepository, platformRepository);
        MusicStreamingResponse result = service.getStreamingPlatforms();

        assertThat(result.platforms()).hasSize(1);
        StreamingPlatformResponse platform = result.platforms().get(0);
        assertThat(platform.name()).isEqualTo("멜론");
        assertThat(platform.osGroups()).hasSize(2);
        assertThat(platform.osGroups().get(0).os()).isEqualTo("ANDROID");
        assertThat(platform.osGroups().get(0).links()).hasSize(2);
        assertThat(platform.osGroups().get(1).os()).isEqualTo("IPHONE");
        assertThat(platform.osGroups().get(1).links()).hasSize(1);
    }

    @Test
    void 플랫폼이_존재하지_않으면_결과에서_제외한다() {
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicStreamingLink link = MusicStreamingLink.of(999L, OperatingSystem.ANDROID, "앱으로 열기", "melon://a", 0);
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of(link));
        when(platformRepository.findById(999L)).thenReturn(Optional.empty());

        MusicStreamingService service = new MusicStreamingService(linkRepository, platformRepository);
        MusicStreamingResponse result = service.getStreamingPlatforms();

        assertThat(result.platforms()).isEmpty();
    }

    @Test
    void 링크가_없으면_빈_리스트를_반환한다() {
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of());

        MusicStreamingService service = new MusicStreamingService(linkRepository, platformRepository);

        assertThat(service.getStreamingPlatforms().platforms()).isEmpty();
    }
}
