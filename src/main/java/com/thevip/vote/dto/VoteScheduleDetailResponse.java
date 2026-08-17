package com.thevip.vote.dto;

import com.thevip.vote.entity.MusicShow;
import java.util.List;

public record VoteScheduleDetailResponse(
        Long id,
        String title,
        String channel,
        String broadcastTime,
        String iconUrl,
        String description,
        List<VoteScheduleRoundResponse> rounds,
        List<VoteDetailGuideResponse> guides) {

    public static VoteScheduleDetailResponse from(MusicShow show, List<VoteScheduleRoundResponse> rounds,
            List<VoteDetailGuideResponse> guides) {
        return new VoteScheduleDetailResponse(
                show.getId(),
                show.getName(),
                show.getChannel(),
                show.getBroadcastTime(),
                show.getIconUrl(),
                show.getDescription(),
                rounds,
                guides);
    }
}
