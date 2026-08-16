package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.entity.MusicCategory;
import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.dto.StreamingPlatformResponse;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.entity.MusicStreamingImage;
import com.thevip.music.entity.MusicStreamingLink;
import com.thevip.music.entity.OperatingSystem;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.music.repository.MusicStreamingImageRepository;
import com.thevip.music.repository.MusicStreamingLinkRepository;
import com.thevip.music.service.MusicStreamingService;
import com.thevip.platform.entity.Platform;
import com.thevip.platform.entity.PlatformRegion;
import com.thevip.platform.entity.PlatformType;
import com.thevip.platform.repository.PlatformRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MusicStreamingServiceTest {

    @Test
    void 플랫폼별_운영체제별로_링크를_묶어서_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());

        MusicStreamingLink androidLink1 = MusicStreamingLink.of(1L, OperatingSystem.ANDROID, "앱으로 열기", "melon://a", 0);
        MusicStreamingLink androidLink2 = MusicStreamingLink.of(1L, OperatingSystem.ANDROID, "웹으로 열기", "https://a", 1);
        MusicStreamingLink iphoneLink = MusicStreamingLink.of(1L, OperatingSystem.IPHONE, "앱으로 열기", "melon://a", 2);
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc())
                .thenReturn(List.of(androidLink1, androidLink2, iphoneLink));

        Platform melon = Platform.of("멜론", PlatformType.MUSIC, PlatformRegion.DOMESTIC, null);
        when(platformRepository.findById(1L)).thenReturn(Optional.of(melon));

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);
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
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());

        MusicStreamingLink link = MusicStreamingLink.of(999L, OperatingSystem.ANDROID, "앱으로 열기", "melon://a", 0);
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of(link));
        when(platformRepository.findById(999L)).thenReturn(Optional.empty());

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);
        MusicStreamingResponse result = service.getStreamingPlatforms();

        assertThat(result.platforms()).isEmpty();
    }

    @Test
    void 링크가_없으면_빈_리스트를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of());

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);

        assertThat(service.getStreamingPlatforms().platforms()).isEmpty();
    }

    @Test
    void 긴급_총공이_있으면_urgent를_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);

        MusicDetail detail = MusicDetail.of(MusicCategory.DOWNLOAD, "테스트 총공", null, null, 0);
        detail.updateUrgentContent("오늘 저녁 12시 30분 멜론 개별곡 다운로드 총공");
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of(detail));
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of());
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);
        MusicStreamingResponse result = service.getStreamingPlatforms();

        assertThat(result.urgent()).isNotNull();
        assertThat(result.urgent().urgentContent()).isEqualTo("오늘 저녁 12시 30분 멜론 개별곡 다운로드 총공");
    }

    @Test
    void 긴급_총공이_없으면_urgent는_null이다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of());
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);

        assertThat(service.getStreamingPlatforms().urgent()).isNull();
    }

    @Test
    void 스트리밍_이미지가_있으면_URL_목록과_최신_업데이트_시각을_반환한다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicStreamingLinkRepository linkRepository = mock(MusicStreamingLinkRepository.class);
        MusicStreamingImageRepository imageRepository = mock(MusicStreamingImageRepository.class);
        PlatformRepository platformRepository = mock(PlatformRepository.class);
        when(musicDetailRepository.findVisibleMenuUrgent(any(LocalDateTime.class))).thenReturn(List.of());
        when(linkRepository.findByActiveTrueOrderByPlatformIdAscSortOrderAsc()).thenReturn(List.of());

        MusicStreamingImage image = MusicStreamingImage.of("https://example.com/playlist.png", 0);
        when(imageRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(image));

        MusicStreamingService service = new MusicStreamingService(
                musicDetailRepository, linkRepository, imageRepository, platformRepository);
        MusicStreamingResponse result = service.getStreamingPlatforms();

        assertThat(result.streamingImageUrls()).containsExactly("https://example.com/playlist.png");
        assertThat(result.imagesUpdatedAt()).isEqualTo(image.getCreatedAt());
    }
}
