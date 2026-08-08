package com.thevip.home.dto;

import com.thevip.music.entity.MusicDetail;
import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;

public record HomeScheduleItemResponse(
        MenuType menuType,
        Long detailId,
        String title,
        LocalDateTime time,
        List<String> platformNames) {

    public static HomeScheduleItemResponse fromMusic(MusicDetail detail, List<String> platformNames) {
        return new HomeScheduleItemResponse(MenuType.MUSIC, detail.getId(), detail.getUrgentContent(),
                detail.getEventAt(), platformNames);
    }

    public static HomeScheduleItemResponse fromVote(VoteDetail detail, List<String> platformNames) {
        return new HomeScheduleItemResponse(MenuType.VOTE, detail.getId(), detail.getTitle(),
                detail.getEventEndAt(), platformNames);
    }
}
