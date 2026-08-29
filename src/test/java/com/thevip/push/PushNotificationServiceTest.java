package com.thevip.push;

import com.thevip.push.service.PushNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PushNotificationServiceTest {

    @Test
    void Firebase_키가_설정되지_않으면_예외없이_발송을_건너뛴다() {
        PushNotificationService service = new PushNotificationService();
        ReflectionTestUtils.setField(service, "serviceAccountKeyBase64", "");

        service.sendToAllUsers("제목", "본문");
    }

    @Test
    void Firebase_키가_설정되지_않으면_예외없이_구독을_건너뛴다() {
        PushNotificationService service = new PushNotificationService();
        ReflectionTestUtils.setField(service, "serviceAccountKeyBase64", "");

        service.subscribeToAllUsers("token-abc");
    }
}
