package com.thevip.push.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.thevip.push.PushTopic;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// FCM 토픽 3개(긴급/음원/투표) 기반. 클라이언트가 알림 수신에 동의하면 발급받은 토큰을 서버로 보내고,
// 서버가 그 토큰을 Member의 카테고리별 설정에 맞는 토픽에 구독시킨다(Member.fcmToken에도 별도 보관).
@Service
@Slf4j
public class PushNotificationService {

    @Value("${firebase.service-account-key-base64:}")
    private String serviceAccountKeyBase64;

    private volatile FirebaseApp firebaseApp;
    private volatile boolean initAttempted;

    // category 콘텐츠 발송. urgent=true면 category 토픽뿐 아니라 긴급 토픽 구독자도 함께 받아야 하는데,
    // 두 토픽에 따로 보내면 둘 다 구독한 사용자가 중복 수신하므로 condition(OR)으로 한 번만 보낸다.
    public void send(PushTopic category, boolean urgent, String title, String body) {
        String condition = urgent && category != PushTopic.URGENT
                ? "'%s' in topics || '%s' in topics".formatted(category.topicName(), PushTopic.URGENT.topicName())
                : "'%s' in topics".formatted(category.topicName());
        sendByCondition(condition, title, body);
    }

    public void subscribe(String token, PushTopic topic) {
        FirebaseApp app = resolveFirebaseApp();
        if (app == null) {
            log.warn("Firebase가 설정되지 않아 토픽 구독을 건너뜁니다. topic={}", topic);
            return;
        }
        try {
            FirebaseMessaging.getInstance(app).subscribeToTopic(List.of(token), topic.topicName());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독 실패. topic={}", topic, e);
        }
    }

    public void unsubscribe(String token, PushTopic topic) {
        FirebaseApp app = resolveFirebaseApp();
        if (app == null) {
            log.warn("Firebase가 설정되지 않아 토픽 구독해제를 건너뜁니다. topic={}", topic);
            return;
        }
        try {
            FirebaseMessaging.getInstance(app).unsubscribeFromTopic(List.of(token), topic.topicName());
        } catch (FirebaseMessagingException e) {
            log.error("토픽 구독해제 실패. topic={}", topic, e);
        }
    }

    private void sendByCondition(String condition, String title, String body) {
        FirebaseApp app = resolveFirebaseApp();
        if (app == null) {
            log.warn("Firebase가 설정되지 않아 푸시 발송을 건너뜁니다. title={}", title);
            return;
        }
        Message message = Message.builder()
                .setCondition(condition)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        try {
            FirebaseMessaging.getInstance(app).send(message);
        } catch (FirebaseMessagingException e) {
            log.error("푸시 발송 실패. title={}", title, e);
        }
    }

    private synchronized FirebaseApp resolveFirebaseApp() {
        if (initAttempted) {
            return firebaseApp;
        }
        initAttempted = true;
        if (serviceAccountKeyBase64 == null || serviceAccountKeyBase64.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(serviceAccountKeyBase64);
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            firebaseApp = FirebaseApp.getApps().isEmpty() ? FirebaseApp.initializeApp(options) : FirebaseApp.getInstance();
        } catch (Exception e) {
            log.error("Firebase 초기화 실패", e);
        }
        return firebaseApp;
    }
}
