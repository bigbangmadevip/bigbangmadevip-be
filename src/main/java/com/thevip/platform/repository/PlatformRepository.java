package com.thevip.platform.repository;

import com.thevip.platform.entity.Platform;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    Optional<Platform> findByName(String name);

    // @OrderColumn 컬렉션(MusicDetail/VoteDetail.platformIds)은 sort_order가 연속이 아니면
    // 빈 인덱스를 null로 채운다 (수동 SQL로 데이터를 넣을 때 흔히 발생). null은 걸러내고 진행한다.
    default List<String> findNamesByIds(List<Long> platformIds) {
        return platformIds.stream()
                .filter(Objects::nonNull)
                .map(this::findById)
                .flatMap(Optional::stream)
                .map(Platform::getName)
                .toList();
    }
}
