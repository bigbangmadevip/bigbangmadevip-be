package com.thevip.schedule.dto;

/**
 * 투표 총공을 캘린더/일별 리스트에서 어떤 날짜(들)에 노출할지 선택하는 기준.
 * EVERY_DAY: 시작일(eventStartAt, 없으면 scheduledAt/createdAt)부터 마감일(eventEndAt)까지 매일 노출.
 * DEADLINE_ONLY: 마감일(eventEndAt) 하루에만 노출.
 */
public enum VoteDisplayMode {
    EVERY_DAY, DEADLINE_ONLY
}
