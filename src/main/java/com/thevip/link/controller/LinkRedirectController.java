package com.thevip.link.controller;

import com.thevip.link.entity.ShortLink;
import com.thevip.link.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequiredArgsConstructor
public class LinkRedirectController {

    private final ShortLinkService shortLinkService;

    /**
     * 앱 스킴(melonapp:// 등)은 인앱 브라우저에서 서버 리다이렉트가 차단되는 경우가 많아
     * 302 대신 브릿지 페이지를 내려준다: JS로 즉시 이동을 시도하고, 실패 시 버튼으로 대체.
     */
    @GetMapping(value = "/s/{shortKey}", produces = MediaType.TEXT_HTML_VALUE)
    public String redirect(@PathVariable String shortKey) {
        ShortLink link = shortLinkService.getByKeyAndRecordClick(shortKey);
        String url = HtmlUtils.htmlEscape(link.getOriginalUrl());
        String title = link.getTitle() == null ? "링크 열기" : HtmlUtils.htmlEscape(link.getTitle());
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>%s</title>
                <style>
                body { font-family: sans-serif; display: flex; flex-direction: column; align-items: center;
                       justify-content: center; min-height: 100vh; margin: 0; gap: 16px; background: #111; color: #fff; }
                a { background: #ffe600; color: #111; padding: 16px 32px; border-radius: 12px;
                    text-decoration: none; font-size: 18px; font-weight: bold; }
                p { color: #999; font-size: 14px; padding: 0 24px; text-align: center; }
                </style>
                </head>
                <body>
                <a id="open" href="%s">%s</a>
                <p>자동으로 열리지 않으면 위 버튼을 눌러주세요.<br>그래도 안 열리면 Safari나 Chrome에서 링크를 열어주세요.</p>
                <script>location.href = document.getElementById('open').href;</script>
                </body>
                </html>
                """.formatted(title, url, title);
    }
}
