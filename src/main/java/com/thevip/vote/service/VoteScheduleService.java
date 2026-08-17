package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.guide.repository.GuideRepository;
import com.thevip.vote.dto.VoteDetailGuideResponse;
import com.thevip.vote.dto.VoteScheduleDetailResponse;
import com.thevip.vote.dto.VoteScheduleListItemResponse;
import com.thevip.vote.dto.VoteScheduleRoundResponse;
import com.thevip.vote.entity.MusicShow;
import com.thevip.vote.repository.MusicShowRepository;
import com.thevip.vote.repository.MusicShowVoteRoundRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteScheduleService {

    private final MusicShowRepository musicShowRepository;
    private final MusicShowVoteRoundRepository musicShowVoteRoundRepository;
    private final GuideRepository guideRepository;

    @Transactional(readOnly = true)
    public List<VoteScheduleListItemResponse> getSchedules() {
        return musicShowRepository.findByActiveTrueOrderBySortOrder().stream()
                .map(VoteScheduleListItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VoteScheduleDetailResponse getSchedule(Long musicShowId) {
        MusicShow show = musicShowRepository.findById(musicShowId)
                .filter(MusicShow::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 투표 플랜입니다."));

        List<VoteScheduleRoundResponse> rounds = musicShowVoteRoundRepository
                .findByMusicShowIdAndActiveTrueOrderBySortOrder(musicShowId).stream()
                .map(VoteScheduleRoundResponse::from)
                .toList();
        List<VoteDetailGuideResponse> guides = guideRepository.findActiveByIds(show.getGuideIds()).stream()
                .map(VoteDetailGuideResponse::from)
                .toList();

        return VoteScheduleDetailResponse.from(show, rounds, guides);
    }
}
