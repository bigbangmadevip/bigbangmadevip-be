package com.thevip.cheering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.thevip.cheering.repository.CheeringRepository;
import com.thevip.cheering.service.CheeringStatsService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
class CheeringStatsServiceCacheTest {

    @Autowired
    CheeringStatsService cheeringStatsService;

    @MockitoSpyBean
    CheeringRepository cheeringRepository;

    @Test
    void 오늘_참여자_수는_캐시되어_두번째_호출부터_DB를_타지_않는다() {
        long first = cheeringStatsService.getTodayParticipantCount();
        long second = cheeringStatsService.getTodayParticipantCount();

        assertThat(first).isEqualTo(second);
        verify(cheeringRepository, times(1)).countDistinctMemberByCheeringDate(LocalDate.now());
    }
}
