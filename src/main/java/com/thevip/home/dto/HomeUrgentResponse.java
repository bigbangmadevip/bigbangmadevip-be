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
        // 배너에는 title(관리용 제목)이 아니라 urgentContent(배너 노출용 문구)를 쓴다.
        // 음원 총공은 마감 개념이 없어 eventEndAt은 항상 null -> 프론트는 "지금 바로 참여해주세요" 고정 문구를 쓴다.
        return new HomeUrgentResponse(MenuType.MUSIC, detail.getId(), detail.getCategory().name(),
                detail.getUrgentContent(), platformNames, null);
    }

    public static HomeUrgentResponse fromVote(VoteDetail detail, List<String> platformNames) {
        // 배너에는 title(관리용 제목)이 아니라 urgentContent(배너 노출용 문구)를 쓴다 (음원과 동일).
        return new HomeUrgentResponse(MenuType.VOTE, detail.getId(), detail.getCategory().name(),
                detail.getUrgentContent(), platformNames, detail.getEventEndAt());
    }
}
