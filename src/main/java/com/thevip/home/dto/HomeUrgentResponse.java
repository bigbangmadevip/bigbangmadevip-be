package com.thevip.home.dto;

import com.thevip.music.entity.MusicDetail;
import com.thevip.vote.entity.VoteDetail;

import java.time.LocalDateTime;
import java.util.List;

public record HomeUrgentResponse(
        MenuType menuType,
        Long detailId,
        String category,
        String title,
        List<String> platformNames,
        LocalDateTime eventEndAt) {

    public static HomeUrgentResponse fromMusic(MusicDetail detail, List<String> platformNames) {
        // 배너에는 title(관리용 제목)이 아니라 urgentContent(배너 노출용 문구)를 쓴다 (투표와 동일).
        return new HomeUrgentResponse(MenuType.MUSIC, detail.getId(), detail.getCategory().name(),
                detail.getUrgentContent(), platformNames, detail.getEventEndAt());
    }

    public static HomeUrgentResponse fromVote(VoteDetail detail, List<String> platformNames) {
        // 배너에는 title(관리용 제목)이 아니라 urgentContent(배너 노출용 문구)를 쓴다 (음원과 동일).
        return new HomeUrgentResponse(MenuType.VOTE, detail.getId(), detail.getCategory().name(),
                detail.getUrgentContent(), platformNames, detail.getEventEndAt());
    }
}
