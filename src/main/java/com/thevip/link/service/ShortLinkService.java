package com.thevip.link.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.link.entity.LinkClick;
import com.thevip.link.entity.ShortLink;
import com.thevip.link.repository.LinkClickRepository;
import com.thevip.link.repository.ShortLinkRepository;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private static final List<String> ALLOWED_SCHEMES = List.of("https://", "http://", "melonapp://");
    private static final String KEY_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 6;

    private final ShortLinkRepository shortLinkRepository;
    private final LinkClickRepository linkClickRepository;
    private final SecureRandom random = new SecureRandom();

    // 어드민 API 연결 전까지는 테스트/데이터 세팅 용도로만 호출됨
    @Transactional
    public ShortLink create(String originalUrl, String title) {
        validateUrl(originalUrl);
        return shortLinkRepository.save(ShortLink.of(generateUniqueKey(), originalUrl, title));
    }

    @Transactional
    public ShortLink getByKeyAndRecordClick(String shortKey) {
        ShortLink link = shortLinkRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 링크입니다."));
        linkClickRepository.save(LinkClick.of(link.getId()));
        return link;
    }

    private void validateUrl(String url) {
        boolean allowed = ALLOWED_SCHEMES.stream().anyMatch(url::startsWith);
        if (!allowed) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "허용되지 않는 URL 형식입니다.");
        }
    }

    private String generateUniqueKey() {
        while (true) {
            StringBuilder sb = new StringBuilder(KEY_LENGTH);
            for (int i = 0; i < KEY_LENGTH; i++) {
                sb.append(KEY_CHARS.charAt(random.nextInt(KEY_CHARS.length())));
            }
            String key = sb.toString();
            if (!shortLinkRepository.existsByShortKey(key)) {
                return key;
            }
        }
    }
}
