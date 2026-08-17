package com.thevip.music.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.music.dto.MusicStreamingImageAdminRequest;
import com.thevip.music.dto.MusicStreamingImageAdminResponse;
import com.thevip.music.entity.MusicStreamingImage;
import com.thevip.music.repository.MusicStreamingImageRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicStreamingImageAdminService {

    private final MusicStreamingImageRepository musicStreamingImageRepository;

    @Transactional(readOnly = true)
    public List<MusicStreamingImageAdminResponse> list() {
        return musicStreamingImageRepository.findAll().stream()
                .sorted(Comparator.comparingInt(MusicStreamingImage::getSortOrder))
                .map(MusicStreamingImageAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicStreamingImageAdminResponse get(Long id) {
        return MusicStreamingImageAdminResponse.from(getEntity(id));
    }

    @Transactional
    public MusicStreamingImageAdminResponse create(MusicStreamingImageAdminRequest request) {
        MusicStreamingImage image = MusicStreamingImage.of(request.imageUrl(), request.sortOrder());
        image.updateActive(request.active());
        musicStreamingImageRepository.save(image);
        return MusicStreamingImageAdminResponse.from(image);
    }

    @Transactional
    public MusicStreamingImageAdminResponse update(Long id, MusicStreamingImageAdminRequest request) {
        MusicStreamingImage image = getEntity(id);
        image.update(request.imageUrl(), request.sortOrder());
        image.updateActive(request.active());
        return MusicStreamingImageAdminResponse.from(image);
    }

    private MusicStreamingImage getEntity(Long id) {
        return musicStreamingImageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 스트리밍 이미지입니다."));
    }
}
