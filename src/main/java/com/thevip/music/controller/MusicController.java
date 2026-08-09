package com.thevip.music.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.music.dto.MusicDetailResponse;
import com.thevip.music.dto.MusicStreamingResponse;
import com.thevip.music.service.MusicDetailService;
import com.thevip.music.service.MusicStreamingService;
import com.thevip.notice.dto.NoticeDetailResponse;
import com.thevip.notice.dto.NoticeListItemResponse;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.service.NoticeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MusicController {

    private final MusicStreamingService musicStreamingService;
    private final MusicDetailService musicDetailService;
    private final NoticeService noticeService;

    @GetMapping("/api/v1/music/streaming")
    public ApiResponse<MusicStreamingResponse> streaming() {
        return ApiResponse.success(musicStreamingService.getStreamingPlatforms());
    }

    @GetMapping("/api/v1/music/detail/{detailId}")
    public ApiResponse<MusicDetailResponse> detail(@PathVariable Long detailId) {
        return ApiResponse.success(musicDetailService.getDetail(detailId));
    }

    @GetMapping("/api/v1/music/notices")
    public ApiResponse<List<NoticeListItemResponse>> notices() {
        return ApiResponse.success(noticeService.getNotices(NoticeMenuType.MUSIC));
    }

    @GetMapping("/api/v1/music/notices/{noticeId}")
    public ApiResponse<NoticeDetailResponse> notice(@PathVariable Long noticeId) {
        return ApiResponse.success(noticeService.getNotice(NoticeMenuType.MUSIC, noticeId));
    }
}
