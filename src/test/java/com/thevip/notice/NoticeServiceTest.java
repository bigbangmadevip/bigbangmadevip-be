package com.thevip.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.global.exception.BusinessException;
import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.repository.NoticeRepository;
import com.thevip.notice.service.NoticeService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NoticeServiceTest {

    @Test
    void 메뉴타입으로_목록을_조회한다() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        Notice notice = Notice.of(NoticeMenuType.MUSIC, "제목", "내용");
        when(noticeRepository.findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(NoticeMenuType.MUSIC))
                .thenReturn(List.of(notice));

        NoticeService service = new NoticeService(noticeRepository);
        List<?> result = service.getNotices(NoticeMenuType.MUSIC);

        assertThat(result).hasSize(1);
    }

    @Test
    void 목록_조회_결과에_고정_여부가_반영된다() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        Notice notice = Notice.of(NoticeMenuType.MUSIC, "제목", "내용");
        notice.updatePinned(true);
        when(noticeRepository.findByMenuTypeAndActiveTrueOrderByPinnedDescCreatedAtDesc(NoticeMenuType.MUSIC))
                .thenReturn(List.of(notice));

        NoticeService service = new NoticeService(noticeRepository);
        var result = service.getNotices(NoticeMenuType.MUSIC);

        assertThat(result.get(0).pinned()).isTrue();
    }

    @Test
    void 상세를_조회하면_이미지_목록도_같이_반환한다() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        Notice notice = Notice.of(NoticeMenuType.MUSIC, "제목", "내용");
        notice.addImageUrl("https://example.com/1.png");
        when(noticeRepository.findByIdAndMenuType(1L, NoticeMenuType.MUSIC)).thenReturn(Optional.of(notice));

        NoticeService service = new NoticeService(noticeRepository);
        var result = service.getNotice(NoticeMenuType.MUSIC, 1L);

        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.imageUrls()).containsExactly("https://example.com/1.png");
    }

    @Test
    void 다른_메뉴타입의_공지는_조회되지_않는다() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        when(noticeRepository.findByIdAndMenuType(1L, NoticeMenuType.VOTE)).thenReturn(Optional.empty());

        NoticeService service = new NoticeService(noticeRepository);

        assertThatThrownBy(() -> service.getNotice(NoticeMenuType.VOTE, 1L))
                .isInstanceOf(BusinessException.class);
    }
}
