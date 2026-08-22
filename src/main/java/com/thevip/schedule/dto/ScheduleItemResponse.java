package com.thevip.schedule.dto;

import com.thevip.home.dto.MenuType;
import com.thevip.music.entity.MusicDetail;
import com.thevip.vote.entity.VoteDetail;
import java.time.LocalDateTime;
import java.util.List;

public record ScheduleItemResponse(
        MenuType menuType,
        Long detailId,
        String title,
        LocalDateTime time,
        List<String> platformNames) {

    public static ScheduleItemResponse fromMusic(MusicDetail detail, List<String> platformNames) {
        return new ScheduleItemResponse(MenuType.MUSIC, detail.getId(), detail.getTitle(),
                detail.getEventStartAt(), platformNames);
    }

    public static ScheduleItemResponse fromVote(VoteDetail detail, List<String> platformNames) {
        return new ScheduleItemResponse(MenuType.VOTE, detail.getId(), detail.getTitle(),
                detail.getEventEndAt(), platformNames);
    }
}
