package com.thevip.guide.repository;

import com.thevip.guide.entity.Guide;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    // @OrderColumn 컬렉션(MusicDetail/VoteDetail.guideIds)은 sort_order가 연속이 아니면
    // 빈 인덱스를 null로 채운다. null은 걸러내고, 비활성/삭제된 가이드도 결과에서 제외한다.
    default List<Guide> findActiveByIds(List<Long> guideIds) {
        return guideIds.stream()
                .filter(Objects::nonNull)
                .map(this::findById)
                .flatMap(Optional::stream)
                .filter(Guide::isActive)
                .toList();
    }
}
