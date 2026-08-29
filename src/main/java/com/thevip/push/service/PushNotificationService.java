package com.thevip.push.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// FCM 토픽 브로드캐스트 방식. 회원별 디바이스 토큰은 서버가 들고 있지 않고, 앱이 알림 수신에 동의하면
// 클라이언트가 직접 Firebase에 "all_users" 토픽을 구독한다. 서버는 그 토픽으로 발송 요청만 보낸다.
@Service
@Slf4j
public class PushNotificationService {

    private static final String ALL_USERS_TOPIC = "all_users";

    @Value("${firebase.service-account-key-base64:}")
    private String serviceAccountKeyBase64;

    private volatile FirebaseApp firebaseApp;
    private volatile boolean initAttempted;

    public void sendToAllUsers(String title, String body) {
        FirebaseApp app = resolveFirebaseApp();
        if (app == null) {
            log.warn("Firebase가 설정되지 않아 푸시 발송을 건너뜁니다. title={}", title);
            return;
        }
        Message message = Message.builder()
                .setTopic(ALL_USERS_TOPIC)
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
