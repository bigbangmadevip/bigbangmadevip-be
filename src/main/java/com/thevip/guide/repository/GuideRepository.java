package com.thevip.guide.repository;

import com.thevip.guide.entity.Guide;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuideRepository extends JpaRepository<Guide, Long> {

    // @OrderColumn 컬렉션(MusicDetail/VoteDetail.guideIds)은 sort_order가 연속이 아니면
    // 빈 인덱스를 null로 채운다. null은 걸러내고, 비활성/삭제된 가이드도 결과에서 제외한다.
    // findAllById로 한 번에 조회 후 원래 순서대로 재조립한다 (건별 findById N+1 방지).
    default List<Guide> findActiveByIds(List<Long> guideIds) {
        List<Long> ids = guideIds.stream().filter(Objects::nonNull).toList();
        Map<Long, Guide> guidesById = findAllById(ids).stream()
                .collect(Collectors.toMap(Guide::getId, guide -> guide));
        return ids.stream().map(guidesById::get).filter(Objects::nonNull).filter(Guide::isActive).toList();
    }
}
