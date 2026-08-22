package com.thevip.music;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thevip.music.dto.MusicDetailAdminRequest;
import com.thevip.music.entity.MusicCategory;
import com.thevip.music.entity.MusicDetail;
import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.music.service.MusicDetailAdminService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MusicDetailAdminServiceTest {

    @Test
    void 긴급배너를_켜면_기존에_켜져있던_다른_상세는_꺼진다() {
        MusicDetailRepository musicDetailRepository = mock(MusicDetailRepository.class);
        MusicDetail existingUrgent = MusicDetail.of(MusicCategory.STREAMING, "기존 긴급", null, null, null, 0);
        existingUrgent.updateMenuUrgent(true);
        ReflectionTestUtils.setField(existingUrgent, "id", 1L);
        when(musicDetailRepository.findById(1L)).thenReturn(Optional.of(existingUrgent));

        MusicDetail newDetail = MusicDetail.of(MusicCategory.STREAMING, "새 긴급", null, null, null, 1);
        ReflectionTestUtils.setField(newDetail, "id", 2L);
        when(musicDetailRepository.findById(2L)).thenReturn(Optional.of(newDetail));
        when(musicDetailRepository.findByMenuUrgentTrue()).thenReturn(List.of(existingUrgent));

        MusicDetailAdminService service = new MusicDetailAdminService(musicDetailRepository);
        MusicDetailAdminRequest request = new MusicDetailAdminRequest(
                MusicCategory.STREAMING, "새 긴급", null, null, null, null, null, null, null,
                true, "새 긴급 배너", true, null, 1);
        service.update(2L, request);

        assertThat(existingUrgent.isMenuUrgent()).isFalse();
        assertThat(newDetail.isMenuUrgent()).isTrue();
    }
}
