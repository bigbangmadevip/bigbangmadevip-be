package com.thevip.platform.repository;

import com.thevip.platform.entity.Platform;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    Optional<Platform> findByName(String name);

    // @OrderColumn 컬렉션(MusicDetail/VoteDetail.platformIds)은 sort_order가 연속이 아니면
    // 빈 인덱스를 null로 채운다 (수동 SQL로 데이터를 넣을 때 흔히 발생). null은 걸러내고 진행한다.
    // findAllById로 한 번에 조회 후 원래 순서대로 재조립한다 (건별 findById N+1 방지).
    default List<String> findNamesByIds(List<Long> platformIds) {
        List<Long> ids = platformIds.stream().filter(Objects::nonNull).toList();
        Map<Long, Platform> platformsById = findAllById(ids).stream()
                .collect(Collectors.toMap(Platform::getId, platform -> platform));
        return ids.stream()
                .map(platformsById::get)
                .filter(Objects::nonNull)
                .filter(Platform::isActive)
                .map(Platform::getName)
                .toList();
    }
}
