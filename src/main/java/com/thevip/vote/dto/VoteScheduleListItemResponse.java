package com.thevip.vote.dto;

import com.thevip.vote.entity.MusicShow;

public record VoteScheduleListItemResponse(Long id, String title, String iconUrl, String broadcastTime) {

    public static VoteScheduleListItemResponse from(MusicShow show) {
        return new VoteScheduleListItemResponse(show.getId(), show.getName(), show.getIconUrl(), show.getBroadcastTime());
    }
}
