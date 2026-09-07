package com.thevip.streaming.service;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// "BiiG" 총공 스트리밍 리스트(18곡)를 처음부터 반복재생한다고 가정하고, 매일 08시/18시/23시에
// 재생 위치가 리스트 1번 트랙(BiiG)으로 돌아가는 리셋 스케줄을 반영해 시작 시점(ANCHOR)부터
// 현재까지 BiiG이 몇 번 재생됐는지 시뮬레이션한다. 실시간 카운팅이 아니라 매시 정각에 갱신해
// 캐싱한 값을 내려준다 - 다음 갱신 전까지는 같은 값을 유지한다.
@Service
@RequiredArgsConstructor
public class BiigStreamCountService {

    private static final LocalDateTime ANCHOR = LocalDateTime.of(2026, 8, 19, 18, 30, 0);
    private static final int[] RESET_HOURS = {8, 18, 23};

    private static final List<Track> TRACKS = List.of(
            new Track("BiiG", 165),
            new Track("HOME SWEET HOME", 212),
            new Track("거짓말", 229),
            new Track("하루하루", 256),
            new Track("BiiG", 165),
            new Track("LIVE FAST DIE SLOW", 181),
            new Track("HOME SWEET HOME", 212),
            new Track("붉은 노을", 208),
            new Track("봄여름가을겨울", 189),
            new Track("BiiG", 165),
            new Track("BANG BANG BANG", 220),
            new Track("한도초과", 192),
            new Track("TOO BAD", 154),
            new Track("BiiG", 165),
            new Track("LIVE FAST DIE SLOW", 181),
            new Track("HOME SWEET HOME", 212),
            new Track("WE LIKE 2 PARTY", 195),
            new Track("마지막 인사", 231));

    private static final int LOOP_SECONDS = TRACKS.stream().mapToInt(Track::durationSeconds).sum();
    private static final List<Integer> BIIG_OFFSETS = biigOffsets();

    private final Clock clock;

    private volatile Long count;
    private volatile LocalDateTime updatedAt;

    public Long getCount() {
        return count;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // 배포(재기동) 직후 정각까지 기다리지 않도록 시작 시점에 한 번 즉시 계산해둔다.
    @PostConstruct
    @Scheduled(cron = "0 0 * * * *")
    public void refresh() {
        LocalDateTime now = LocalDateTime.now(clock);
        count = calculate(now);
        updatedAt = now;
    }

    public long calculate(LocalDateTime now) {
        long total = 0;
        LocalDateTime cursor = ANCHOR;
        while (cursor.isBefore(now)) {
            LocalDateTime nextReset = nextResetAfter(cursor);
            LocalDateTime segmentEnd = nextReset.isBefore(now) ? nextReset : now;
            total += biigCountInSpan(Duration.between(cursor, segmentEnd).getSeconds());
            cursor = segmentEnd;
        }
        return total;
    }

    private long biigCountInSpan(long spanSeconds) {
        long fullLoops = spanSeconds / LOOP_SECONDS;
        long remainder = spanSeconds % LOOP_SECONDS;
        long partial = BIIG_OFFSETS.stream().filter(offset -> offset <= remainder).count();
        return fullLoops * BIIG_OFFSETS.size() + partial;
    }

    private static LocalDateTime nextResetAfter(LocalDateTime time) {
        LocalDate day = time.toLocalDate();
        for (int hour : RESET_HOURS) {
            LocalDateTime candidate = day.atTime(hour, 0);
            if (candidate.isAfter(time)) {
                return candidate;
            }
        }
        return day.plusDays(1).atTime(RESET_HOURS[0], 0);
    }

    private static List<Integer> biigOffsets() {
        List<Integer> offsets = new ArrayList<>();
        int offset = 0;
        for (Track track : TRACKS) {
            if (track.name().equals("BiiG")) {
                offsets.add(offset);
            }
            offset += track.durationSeconds();
        }
        return offsets;
    }

    private record Track(String name, int durationSeconds) {
    }
}
