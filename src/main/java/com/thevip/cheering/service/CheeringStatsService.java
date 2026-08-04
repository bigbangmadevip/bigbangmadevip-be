package com.thevip.cheering.service;

import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.global.config.CacheConfig;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheeringStatsService {

    private final CheeringRepository cheeringRepository;

    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.TODAY_PARTICIPANT_COUNT)
    public long getTodayParticipantCount() {
        return cheeringRepository.countDistinctMemberByCheeringDate(LocalDate.now());
    }
}
