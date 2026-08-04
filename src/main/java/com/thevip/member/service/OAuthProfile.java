package com.thevip.member.service;

import com.thevip.member.entity.Provider;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public record OAuthProfile(Provider provider, String providerId, String name) {

    @SuppressWarnings("unchecked")
    public static OAuthProfile from(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> {
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                String nickname = properties != null && properties.get("nickname") != null
                        ? (String) properties.get("nickname")
                        : "VIP";
                yield new OAuthProfile(Provider.KAKAO, String.valueOf(attributes.get("id")), nickname);
            }
            default -> throw new OAuth2AuthenticationException("unsupported_provider: " + registrationId);
        };
    }
}
