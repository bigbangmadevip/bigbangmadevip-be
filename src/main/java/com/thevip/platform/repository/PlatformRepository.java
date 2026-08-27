package com.thevip.platform.repository;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import com.thevip.platform.entity.Platform;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    Optional<Platform> findByName(String name);

    Optional<Platform> findByCode(String code);

    List<Platform> findByCodeIn(List<String> codes);

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

    // 어드민 응답용 id -> code 변환. 공개용 findNamesByIds와 달리 비활성 플랫폼도 그대로 보여준다 —
    // 어드민이 저장된 값을 다시 저장할 때 비활성이라는 이유로 조용히 빠지면 안 되기 때문이다.
    default List<String> findCodesByIds(List<Long> platformIds) {
        List<Long> ids = platformIds.stream().filter(Objects::nonNull).toList();
        Map<Long, Platform> platformsById = findAllById(ids).stream()
                .collect(Collectors.toMap(Platform::getId, platform -> platform));
        return ids.stream()
                .map(platformsById::get)
                .filter(Objects::nonNull)
                .map(Platform::getCode)
                .toList();
    }

    // 어드민 요청용 code -> id 변환. 존재하지 않는 code가 오면 명확히 에러를 낸다 — 오타로 조용히
    // 새 플랫폼이 생기면 안 되므로 자동 생성은 하지 않고, 먼저 플랫폼 어드민 API로 등록해야 한다.
    default List<Long> resolveIdsByCodes(List<String> codes) {
        Map<String, Platform> platformsByCode = findByCodeIn(codes).stream()
                .collect(Collectors.toMap(Platform::getCode, platform -> platform));
        return codes.stream()
                .map(code -> {
                    Platform platform = platformsByCode.get(code);
                    if (platform == null) {
                        throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 플랫폼 코드입니다: " + code);
                    }
                    return platform.getId();
                })
                .toList();
    }
}
