package com.thevip.notice.dto;

import com.thevip.notice.entity.NoticeLink;

public record NoticeLinkResponse(String label, String url) {

    public static NoticeLinkResponse from(NoticeLink link) {
        return new NoticeLinkResponse(link.getLabel(), link.getUrl());
    }
}
