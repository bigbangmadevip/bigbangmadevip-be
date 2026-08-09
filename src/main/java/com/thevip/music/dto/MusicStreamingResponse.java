package com.thevip.music.dto;

import java.util.List;

public record MusicStreamingResponse(List<StreamingPlatformResponse> platforms) {
}
