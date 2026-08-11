package com.thevip.schedule.service;

import com.thevip.music.repository.MusicDetailRepository;
import com.thevip.platform.repository.PlatformRepository;
import com.thevip.schedule.dto.ScheduleCategory;
import com.thevip.schedule.dto.ScheduleDayCountResponse;
import com.thevip.schedule.dto.ScheduleDayResponse;
import com.thevip.schedule.dto.ScheduleItemResponse;
import com.thevip.schedule.dto.ScheduleMonthResponse;
import com.thevip.schedule.dto.VoteDisplayMode;
import com.thevip.vote.entity.VoteDetail;
import com.thevip.vote.repository.VoteDetailRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final MusicDetailRepository musicDetailRepository;
    private final VoteDetailRepository voteDetailRepository;
    private final PlatformRepository platformRepository;

    @Transactional(readOnly = true)
    public ScheduleMonthResponse getMonth(YearMonth yearMonth, ScheduleCategory category, VoteDisplayMode voteDisplayMode) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDateTime rangeStart = monthStart.atStartOfDay();
        LocalDateTime rangeEnd = monthEnd.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        Map<LocalDate, int[]> counts = new TreeMap<>();

        if (category != ScheduleCategory.VOTE) {
            musicDetailRepository.findActiveInRange(now, rangeStart, rangeEnd)
                    .forEach(detail -> counts.computeIfAbsent(detail.getEventAt().toLocalDate(), d -> new int[2])[0]++);
        }
        if (category != ScheduleCategory.MUSIC) {
            if (voteDisplayMode == VoteDisplayMode.DEADLINE_ONLY) {
                voteDetailRepository.findActiveByDeadlineInRange(now, rangeStart, rangeEnd)
                        .forEach(detail -> counts.computeIfAbsent(detail.getEventEndAt().toLocalDate(), d -> new int[2])[1]++);
            } else {
                voteDetailRepository.findActiveOverlapping(now, rangeStart, rangeEnd)
                        .forEach(detail -> voteDaysInRange(detail, monthStart, monthEnd)
                                .forEach(day -> counts.computeIfAbsent(day, d -> new int[2])[1]++));
            }
        }

        List<ScheduleDayCountResponse> days = counts.entrySet().stream()
                .map(entry -> new ScheduleDayCountResponse(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .toList();
        return new ScheduleMonthResponse(days);
    }

    @Transactional(readOnly = true)
    public ScheduleDayResponse getDay(LocalDate date, ScheduleCategory category, VoteDisplayMode voteDisplayMode) {
        LocalDateTime rangeStart = date.atStartOfDay();
        LocalDateTime rangeEnd = date.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<ScheduleItemResponse> items = new ArrayList<>();
        if (category != ScheduleCategory.VOTE) {
            musicDetailRepository.findActiveInRange(now, rangeStart, rangeEnd)
                    .forEach(detail -> items.add(ScheduleItemResponse.fromMusic(detail,
                            platformRepository.findNamesByIds(detail.getPlatformIds()))));
        }
        if (category != ScheduleCategory.MUSIC) {
            List<VoteDetail> votes = voteDisplayMode == VoteDisplayMode.DEADLINE_ONLY
                    ? voteDetailRepository.findActiveByDeadlineInRange(now, rangeStart, rangeEnd)
                    : voteDetailRepository.findActiveOverlapping(now, rangeStart, rangeEnd);
            votes.forEach(detail -> items.add(ScheduleItemResponse.fromVote(detail,
                    platformRepository.findNamesByIds(detail.getPlatformIds()))));
        }

        List<ScheduleItemResponse> sorted = items.stream()
                .sorted(Comparator.comparing(ScheduleItemResponse::time))
                .toList();
        return new ScheduleDayResponse(date, sorted);
    }

    // EVERY_DAY 모드에서, 투표가 시작일부터 마감일까지 매일 진행 중이라고 보고 걸치는 날짜를 전부 펼친다.
    private List<LocalDate> voteDaysInRange(VoteDetail detail, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDateTime spanStartAt = detail.getEventStartAt() != null ? detail.getEventStartAt()
                : detail.getScheduledAt() != null ? detail.getScheduledAt() : detail.getCreatedAt();
        LocalDate spanStart = spanStartAt.toLocalDate();
        LocalDate spanEnd = detail.getEventEndAt().toLocalDate();

        LocalDate from = spanStart.isBefore(rangeStart) ? rangeStart : spanStart;
        LocalDate to = spanEnd.isAfter(rangeEnd) ? rangeEnd : spanEnd;

        List<LocalDate> days = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            days.add(day);
        }
        return days;
    }
}
