package com.thevip.streaming;

import static org.assertj.core.api.Assertions.assertThat;

import com.thevip.streaming.service.TitleStreamCountService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TitleStreamCountServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Test
    void 시작_시점부터_경과한_리셋_구간을_반영해_타이틀곡_재생_횟수를_계산한다() {
        Clock clock = fixedClockAt(2026, 9, 7, 22, 0);
        TitleStreamCountService service = new TitleStreamCountService(clock);

        long count = service.calculate(LocalDateTime.of(2026, 9, 7, 22, 0));

        assertThat(count).isEqualTo(1896);
    }

    @Test
    void 리셋_시각을_지나면_횟수가_늘어난다() {
        Clock clock = fixedClockAt(2026, 9, 7, 23, 0);
        TitleStreamCountService service = new TitleStreamCountService(clock);

        long count = service.calculate(LocalDateTime.of(2026, 9, 7, 23, 0));

        assertThat(count).isEqualTo(1900);
    }

    @Test
    void 시작_시점_직후에는_1회다() {
        Clock clock = fixedClockAt(2026, 8, 19, 18, 31);
        TitleStreamCountService service = new TitleStreamCountService(clock);

        long count = service.calculate(LocalDateTime.of(2026, 8, 19, 18, 31));

        assertThat(count).isEqualTo(1);
    }

    @Test
    void 매시_정각_스케줄러가_갱신한값을_캐싱한다() {
        Clock clock = fixedClockAt(2026, 9, 7, 22, 0);
        TitleStreamCountService service = new TitleStreamCountService(clock);

        service.refresh();

        assertThat(service.getCount()).isEqualTo(1896);
        assertThat(service.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 7, 22, 0));
    }

    private static Clock fixedClockAt(int year, int month, int day, int hour, int minute) {
        return Clock.fixed(LocalDateTime.of(year, month, day, hour, minute).atZone(ZONE).toInstant(), ZONE);
    }
}
