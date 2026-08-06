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
        String songName,
        Long platformId,
        List<String> checklist,
        String imageUrl,
        LocalDateTime eventEndAt) {

    public static HomeUrgentResponse fromMusic(MusicDetail detail) {
        String firstImageUrl = detail.getImageUrls().isEmpty() ? null : detail.getImageUrls().get(0);
        // checklist는 지연 로딩 컬렉션이라, 트랜잭션이 끝나고 나중에 직렬화될 때 접근하면
        // LazyInitializationException이 난다. 여기서(트랜잭션 안) 바로 복사해서 담아둔다.
        List<String> checklist = List.copyOf(detail.getChecklist());
        // 음원 총공은 마감 개념이 없어 eventEndAt은 항상 null -> 프론트는 "지금 바로 참여해주세요" 고정 문구를 쓴다.
        return new HomeUrgentResponse(MenuType.MUSIC, detail.getId(), detail.getCategory().name(), detail.getTitle(),
                detail.getSongName(), detail.getPlatformId(), checklist, firstImageUrl, null);
    }

    public static HomeUrgentResponse fromVote(VoteDetail detail) {
        String firstImageUrl = detail.getImageUrls().isEmpty() ? null : detail.getImageUrls().get(0);
        List<String> checklist = List.copyOf(detail.getChecklist());
        return new HomeUrgentResponse(MenuType.VOTE, detail.getId(), detail.getCategory().name(), detail.getTitle(),
                null, detail.getPlatformId(), checklist, firstImageUrl, detail.getEventEndAt());
    }
}
