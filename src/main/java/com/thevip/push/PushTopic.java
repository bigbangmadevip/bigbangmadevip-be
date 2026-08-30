package com.thevip.push;

// FCM 토픽 3개(긴급/음원/투표). 실제 FCM 토픽명은 enum 이름과 별개로 소문자로 둔다.
public enum PushTopic {
    URGENT("urgent"),
    MUSIC("music"),
    VOTE("vote");

    private final String topicName;

    PushTopic(String topicName) {
        this.topicName = topicName;
    }

    public String topicName() {
        return topicName;
    }
}
