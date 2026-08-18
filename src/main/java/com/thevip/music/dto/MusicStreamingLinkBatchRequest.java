package com.thevip.music.dto;

import com.thevip.music.entity.OperatingSystem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// 플랫폼 하나의 원클릭 스트리밍 링크 전체(운영체제별 순서 리스트)를 한 번에 등록/교체할 때 쓴다.
public record MusicStreamingLinkBatchRequest(@NotEmpty @Valid List<OsGroupRequest> osGroups) {

    public record OsGroupRequest(@NotNull OperatingSystem os, @NotEmpty @Valid List<LinkRequest> links) {
    }

    public record LinkRequest(@NotBlank String label, @NotBlank String url) {
    }
}
