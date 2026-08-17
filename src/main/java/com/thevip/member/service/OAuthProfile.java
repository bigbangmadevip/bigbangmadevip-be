package com.thevip.member.service;

import com.thevip.member.entity.Provider;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public record OAuthProfile(Provider provider, String providerId, String name, String email) {

    @SuppressWarnings("unchecked")
    public static OAuthProfile from(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> {
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                String nickname = properties != null && properties.get("nickname") != null
                        ? (String) properties.get("nickname")
                        : "VIP";
                // account_email 동의를 안 했거나 카카오가 이메일을 안 준 경우 kakao_account 자체가 없거나
                // email 키가 없을 수 있어 null로 둔다.
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                yield new OAuthProfile(Provider.KAKAO, String.valueOf(attributes.get("id")), nickname, email);
            }
            default -> throw new OAuth2AuthenticationException("unsupported_provider: " + registrationId);
        };
    }
}
