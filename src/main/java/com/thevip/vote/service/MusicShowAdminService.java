package com.thevip.vote.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.vote.dto.MusicShowAdminRequest;
import com.thevip.vote.dto.MusicShowAdminResponse;
import com.thevip.vote.dto.MusicShowVoteRoundAdminRequest;
import com.thevip.vote.dto.MusicShowVoteRoundAdminResponse;
import com.thevip.vote.dto.VoteRoundRowRequest;
import com.thevip.vote.entity.MusicShow;
import com.thevip.vote.entity.MusicShowVoteRound;
import com.thevip.vote.entity.VoteRoundRow;
import com.thevip.vote.repository.MusicShowRepository;
import com.thevip.vote.repository.MusicShowVoteRoundRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicShowAdminService {

    private final MusicShowRepository musicShowRepository;
    private final MusicShowVoteRoundRepository musicShowVoteRoundRepository;

    @Transactional(readOnly = true)
    public List<MusicShowAdminResponse> list() {
        return musicShowRepository.findAll().stream()
                .sorted(Comparator.comparingInt(MusicShow::getSortOrder))
                .map(MusicShowAdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MusicShowAdminResponse get(Long id) {
        return MusicShowAdminResponse.from(getShow(id));
    }

    @Transactional
    public MusicShowAdminResponse create(MusicShowAdminRequest request) {
        MusicShow show = MusicShow.of(request.name(), request.sortOrder());
        applyRequest(show, request);
        musicShowRepository.save(show);
        return MusicShowAdminResponse.from(show);
    }

    @Transactional
    public MusicShowAdminResponse update(Long id, MusicShowAdminRequest request) {
        MusicShow show = getShow(id);
        show.updateName(request.name());
        show.updateSortOrder(request.sortOrder());
        applyRequest(show, request);
        return MusicShowAdminResponse.from(show);
    }

    private void applyRequest(MusicShow show, MusicShowAdminRequest request) {
        show.replacePlatformIds(nullSafe(request.platformIds()));
        show.replaceGuideIds(nullSafe(request.guideIds()));
        show.updateActive(request.active());
        show.updateChannel(request.channel());
        show.updateBroadcastTime(request.broadcastTime());
        show.updateIconUrl(request.iconUrl());
        show.updateDescription(request.description());
    }

    @Transactional(readOnly = true)
    public List<MusicShowVoteRoundAdminResponse> listRounds(Long showId) {
        getShow(showId);
        return musicShowVoteRoundRepository.findByMusicShowIdOrderBySortOrder(showId).stream()
                .map(MusicShowVoteRoundAdminResponse::from)
                .toList();
    }

    @Transactional
    public MusicShowVoteRoundAdminResponse createRound(Long showId, MusicShowVoteRoundAdminRequest request) {
        getShow(showId);
        MusicShowVoteRound round = MusicShowVoteRound.of(showId, request.label(), request.time(), request.tone(),
                request.sortOrder());
        applyRoundRequest(round, request);
        musicShowVoteRoundRepository.save(round);
        return MusicShowVoteRoundAdminResponse.from(round);
    }

    @Transactional
    public MusicShowVoteRoundAdminResponse updateRound(Long showId, Long roundId,
            MusicShowVoteRoundAdminRequest request) {
        MusicShowVoteRound round = musicShowVoteRoundRepository.findByIdAndMusicShowId(roundId, showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 투표 라운드입니다."));
        round.updateCore(request.label(), request.time(), request.tone(), request.sortOrder());
        applyRoundRequest(round, request);
        return MusicShowVoteRoundAdminResponse.from(round);
    }

    private void applyRoundRequest(MusicShowVoteRound round, MusicShowVoteRoundAdminRequest request) {
        round.updateActive(request.active());
        List<VoteRoundRow> rows = nullSafe(request.rows()).stream()
                .map(row -> new VoteRoundRow(row.label(), row.value()))
                .toList();
        round.replaceRows(rows);
    }

    private MusicShow getShow(Long id) {
        return musicShowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 방송입니다."));
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
