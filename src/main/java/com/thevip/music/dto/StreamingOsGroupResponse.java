package com.thevip.music.dto;

import java.util.List;

public record StreamingOsGroupResponse(String os, List<StreamingLinkResponse> links) {
}
