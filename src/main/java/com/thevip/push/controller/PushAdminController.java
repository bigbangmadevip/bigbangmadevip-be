package com.thevip.push.controller;

import com.thevip.global.response.ApiResponse;
import com.thevip.push.PushTopic;
import com.thevip.push.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 카테고리 토픽으로 실제 발송 연동을 확인해보기 위한 테스트용 API.
@RestController
@RequiredArgsConstructor
public class PushAdminController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/api/v1/admin/push/test")
    public ApiResponse<Void> sendTestPush(
            @RequestParam PushTopic topic,
            @RequestParam(defaultValue = "테스트 알림") String title,
            @RequestParam(defaultValue = "테스트 발송입니다") String body) {
        pushNotificationService.send(topic, false, title, body);
        return ApiResponse.success();
    }
}
