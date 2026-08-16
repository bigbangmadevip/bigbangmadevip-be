-- 어드민 화면이 아직 없어서 수동으로 데이터를 넣을 때 참고하는 예시 INSERT 모음.
-- MySQL 기준. boolean은 1/0으로 표기한다.
-- 자동증가 id를 참조해야 하는 곳은 LAST_INSERT_ID()로 직전에 넣은 행의 id를 그대로 받아서 쓴다.
-- 이미 존재하는 platform/guide/cheering_item을 재사용할 거면 해당 INSERT는 건너뛰고
-- SET @xxx_id = <실제 id>; 로 직접 대입해서 쓰면 된다.

-- ============================================================
-- platform
-- ============================================================
-- type: MUSIC | VOTE, region: DOMESTIC | GLOBAL
INSERT INTO platform (name, type, region, icon_url) VALUES ('멜론', 'MUSIC', 'DOMESTIC', NULL);
SET @platform_melon_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('하이어(Higher)', 'VOTE', 'DOMESTIC', NULL);
SET @platform_higher_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('벅스(Bugs)', 'MUSIC', 'DOMESTIC', NULL);
SET @platform_bugs_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('지니', 'MUSIC', 'DOMESTIC', NULL);
SET @platform_genie_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('플로', 'MUSIC', 'DOMESTIC', NULL);
SET @platform_flo_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('바이브', 'MUSIC', 'DOMESTIC', NULL);
SET @platform_vibe_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('스포티파이', 'MUSIC', 'GLOBAL', NULL);
SET @platform_spotify_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('애플뮤직', 'MUSIC', 'GLOBAL', NULL);
SET @platform_apple_music_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('유튜브뮤직', 'MUSIC', 'GLOBAL', NULL);
SET @platform_youtube_music_id = LAST_INSERT_ID();

-- ============================================================
-- music_streaming_link (원클릭 스트리밍: 플랫폼 -> 운영체제 -> 링크 목록)
-- ============================================================
-- os: ANDROID | IPHONE | IPAD | WINDOWS | MAC
-- 같은 platform_id + os 조합에 링크를 여러 개 넣을 수 있다 (sort_order로 정렬).
-- 앱 스킴/URL은 전부 예시 placeholder다 - 실제 배포 전에 각 앱의 실제 딥링크로 검증/교체 필요.
INSERT INTO music_streaming_link (platform_id, os, label, url, active, sort_order, created_at) VALUES
  (@platform_melon_id, 'ANDROID', '멜론 앱으로 스트리밍', 'melonapp://streaming', 1, 0, NOW()),
  (@platform_melon_id, 'ANDROID', '멜론 웹으로 스트리밍', 'https://www.melon.com/', 1, 1, NOW()),
  (@platform_melon_id, 'IPHONE', '멜론 앱으로 스트리밍', 'melonapp://streaming', 1, 0, NOW()),
  (@platform_melon_id, 'IPAD', '멜론 앱으로 스트리밍', 'melonapp://streaming', 1, 0, NOW()),

  (@platform_genie_id, 'ANDROID', '지니 앱으로 스트리밍', 'geniemusic://streaming', 1, 0, NOW()),
  (@platform_genie_id, 'IPHONE', '지니 앱으로 스트리밍', 'geniemusic://streaming', 1, 0, NOW()),

  (@platform_bugs_id, 'ANDROID', '벅스 앱으로 스트리밍', 'bugsapp://streaming', 1, 0, NOW()),
  (@platform_bugs_id, 'IPHONE', '벅스 앱으로 스트리밍', 'bugsapp://streaming', 1, 0, NOW()),

  (@platform_flo_id, 'ANDROID', '플로 앱으로 스트리밍', 'flomusic://streaming', 1, 0, NOW()),
  (@platform_flo_id, 'IPHONE', '플로 앱으로 스트리밍', 'flomusic://streaming', 1, 0, NOW()),

  (@platform_vibe_id, 'ANDROID', '바이브 앱으로 스트리밍', 'vibe://streaming', 1, 0, NOW()),
  (@platform_vibe_id, 'IPHONE', '바이브 앱으로 스트리밍', 'vibe://streaming', 1, 0, NOW()),

  (@platform_spotify_id, 'ANDROID', '스포티파이 앱으로 스트리밍', 'spotify://', 1, 0, NOW()),
  (@platform_spotify_id, 'IPHONE', '스포티파이 앱으로 스트리밍', 'spotify://', 1, 0, NOW()),

  (@platform_apple_music_id, 'IPHONE', '애플뮤직 앱으로 스트리밍', 'https://music.apple.com/', 1, 0, NOW()),
  (@platform_apple_music_id, 'MAC', '애플뮤직 앱으로 스트리밍', 'https://music.apple.com/', 1, 0, NOW()),

  (@platform_youtube_music_id, 'ANDROID', '유튜브뮤직 앱으로 스트리밍', 'https://music.youtube.com/', 1, 0, NOW()),
  (@platform_youtube_music_id, 'IPHONE', '유튜브뮤직 앱으로 스트리밍', 'https://music.youtube.com/', 1, 0, NOW());

-- ============================================================
-- music_streaming_image (원클릭 스트리밍 화면 하단 "스트리밍 리스트" 추천 플레이리스트 이미지)
-- ============================================================
-- 특정 총공(music_detail)과 무관한 탭 공용 콘텐츠. 이미지 URL은 예시 placeholder다.
INSERT INTO music_streaming_image (image_url, active, sort_order, created_at) VALUES
  ('https://example.com/music/streaming/playlist-1.png', 1, 0, NOW());

-- ============================================================
-- guide (+ guide_image)
-- ============================================================
-- guide_type: STREAMING | DOWNLOAD | MV_REPEAT_PLAY | ID_CREATION
INSERT INTO guide (guide_type, platform_id, title, active, sort_order, created_at)
VALUES ('DOWNLOAD', @platform_melon_id, '멜론 다운로드 방법 안내', 1, 0, NOW());
SET @guide_id = LAST_INSERT_ID();

INSERT INTO guide_image (guide_id, sort_order, image_url) VALUES
  (@guide_id, 0, 'https://example.com/guide/1.png'),
  (@guide_id, 1, 'https://example.com/guide/2.png');

-- ============================================================
-- cheering_item ("오늘의 응원" 카탈로그 항목. music_detail/vote_detail의 cheering_item_id가 이걸 가리킨다)
-- ============================================================
-- category: STREAMING | DOWNLOAD | VOTE | YOUTUBE | VOTECOIN | REPORT | HASHTAG
INSERT INTO cheering_item (category, title, subtitle, sort_order, active)
VALUES ('STREAMING', '음원\n스트리밍', '멜론에서 스트리밍하기', 0, 1);
SET @cheering_item_id = LAST_INSERT_ID();

-- ============================================================
-- music_detail (+ music_detail_checklist / music_detail_image / music_detail_guide)
-- ============================================================
-- category: DOWNLOAD | STREAMING | ETC
-- menu_urgent=1로 하면 홈 화면 긴급 배너 후보가 된다 (음원/투표 통틀어 최대 1개만 켜져 있어야 함, 어드민이 직접 관리).
-- today_exposed=1로 하면 홈 화면 "오늘의 총공 일정" 리스트에 노출된다 (eventAt이 오늘 날짜일 때만, 최대 5개).
-- 이 둘은 완전히 별개 필드라 필요에 따라 하나만 켜거나 둘 다 켜도 된다.
-- urgent_content는 최대 26자, 긴급 배너/오늘의 총공 일정에 보여줄 짧은 문구 (title과 별개).
-- scheduled_at을 지정하면 그 시각이 지나기 전까지는 active=1이어도 노출에서 제외된다 (NULL이면 즉시 노출).
-- platform_id는 단일 컬럼이 아니라 music_detail_platform 조인 테이블로 여러 개 등록 가능 (멜론+벅스 동시 총공 등).
INSERT INTO music_detail (
    category, title, song_name, platform_url, event_at, description,
    cheering_item_id, menu_urgent, today_exposed, urgent_content, active, scheduled_at, sort_order,
    created_at
) VALUES (
    'DOWNLOAD', '오늘 저녁 8시 30분 멜론 개별곡 다운로드 총공', '타이틀 곡 <봄여름가을겨울>',
    'https://www.melon.com/', CONCAT(CURDATE(), ' 20:30:00'), '총공 상세 설명',
    @cheering_item_id, 1, 1, '오늘 저녁 8시 30분 멜론 다운로드 총공', 1, NULL, 0,
    NOW()
);
SET @music_detail_id = LAST_INSERT_ID();

INSERT INTO music_detail_platform (music_detail_id, sort_order, platform_id) VALUES
  (@music_detail_id, 0, @platform_melon_id),
  (@music_detail_id, 1, @platform_bugs_id);

INSERT INTO music_detail_checklist (music_detail_id, sort_order, content) VALUES
  (@music_detail_id, 0, 'Too Bad, Home sweet Home, Live Fast Die Slow 스트리밍 필수'),
  (@music_detail_id, 1, '다운로드 파일 삭제 확인 후 진행');

INSERT INTO music_detail_image (music_detail_id, sort_order, image_url) VALUES
  (@music_detail_id, 0, 'https://example.com/music/1.png');

INSERT INTO music_detail_guide (music_detail_id, sort_order, guide_id) VALUES
  (@music_detail_id, 0, @guide_id);

-- ============================================================
-- notice (음원/투표 메뉴 공용, menu_type으로 구분. + notice_image)
-- ============================================================
-- menu_type: MUSIC | VOTE
-- updated_by/updated_at은 화면에 노출하지 않는 관리용 이력 (누가 마지막으로 고쳤는지).
INSERT INTO notice (menu_type, title, content, active, created_at)
VALUES ('MUSIC', '음원 공지 제목', '음원 공지 내용', 1, NOW());
SET @music_notice_id = LAST_INSERT_ID();

INSERT INTO notice_image (notice_id, sort_order, image_url) VALUES
  (@music_notice_id, 0, 'https://example.com/music/notice/1.png');

-- ============================================================
-- music_show (+ music_show_platform) — 투표 총공 등록 화면의 "총공 구분"(음악방송 프로그램).
-- 이 방송을 고르면 여기 등록된 platform_id들이 "총공 플랫폼" 선택지로 노출된다.
-- ============================================================
INSERT INTO platform (name, type, region, icon_url) VALUES ('뮤빗', 'VOTE', 'DOMESTIC', NULL);
SET @platform_mubeat_id = LAST_INSERT_ID();

INSERT INTO platform (name, type, region, icon_url) VALUES ('뮤니버스', 'VOTE', 'DOMESTIC', NULL);
SET @platform_muniverse_id = LAST_INSERT_ID();

INSERT INTO music_show (name, active, sort_order, created_at) VALUES ('쇼! 음악중심', 1, 0, NOW());
SET @music_show_id = LAST_INSERT_ID();

INSERT INTO music_show_platform (music_show_id, sort_order, platform_id) VALUES
  (@music_show_id, 0, @platform_mubeat_id),
  (@music_show_id, 1, @platform_muniverse_id);

-- ============================================================
-- vote_detail (+ vote_detail_checklist / vote_detail_image / vote_detail_guide)
-- ============================================================
-- category: AWARDS | MUSIC_SHOW | ANNIVERSARY | ETC
-- music_show_id는 category=MUSIC_SHOW일 때 "총공 구분"으로 고른 music_show를 가리킨다 (선택, ID 참조).
-- menu_urgent=1로 하면 홈 화면 긴급 배너 후보가 된다 (음원/투표 통틀어 최대 1개만, 어드민이 직접 관리).
-- urgent_content는 최대 26자, 긴급 배너에 보여줄 짧은 문구 (title과 별개). menu_urgent=1일 때만 의미 있음.
-- today_exposed=1로 하면 홈 화면 "오늘의 총공 일정" 리스트에 노출된다 (eventEndAt이 오늘 날짜일 때만, 최대 5개).
-- platform_id는 단일 컬럼이 아니라 vote_detail_platform 조인 테이블로 여러 개 등록 가능.
-- push_*는 설정값만 저장한다 (실제 푸시 발송 연동은 아직 없음). push_send_at이 NULL이면 "게시 즉시" 의미.
INSERT INTO vote_detail (
    category, title, music_show_id, reward_description, platform_url,
    event_start_at, event_end_at, cta_button_label,
    cheering_item_id, menu_urgent, urgent_content, today_exposed, active, scheduled_at, sort_order,
    push_enabled, push_send_at, push_title, push_body,
    created_at
) VALUES (
    'MUSIC_SHOW', '인기가요 생방송 투표', NULL, '1위 시 팬사인회 진행', 'https://example.com/vote',
    NULL, CONCAT(CURDATE(), ' 23:59:00'), '투표하러 가기',
    NULL, 0, NULL, 1, 1, NULL, 0,
    0, NULL, NULL, NULL,
    NOW()
);
SET @vote_detail_id = LAST_INSERT_ID();

INSERT INTO vote_detail_platform (vote_detail_id, sort_order, platform_id) VALUES
  (@vote_detail_id, 0, @platform_higher_id);

INSERT INTO vote_detail_checklist (vote_detail_id, sort_order, content) VALUES
  (@vote_detail_id, 0, '하루 최대 투표 가능 횟수 확인');

INSERT INTO vote_detail_image (vote_detail_id, sort_order, image_url) VALUES
  (@vote_detail_id, 0, 'https://example.com/vote/1.png');

INSERT INTO vote_detail_guide (vote_detail_id, sort_order, guide_id) VALUES
  (@vote_detail_id, 0, @guide_id);

-- ============================================================
-- notice (VOTE)
-- ============================================================
INSERT INTO notice (menu_type, title, content, active, created_at)
VALUES ('VOTE', '투표 공지 제목', '투표 공지 내용', 1, NOW());

-- ============================================================
-- member (보통 카카오/구글/네이버 로그인 시 자동 생성되므로 수동 삽입은 테스트용)
-- ============================================================
-- provider: KAKAO | GOOGLE | NAVER | APPLE, role: USER | ADMIN
INSERT INTO member (provider, provider_id, name, nickname, role, created_at)
VALUES ('KAKAO', '1234567890', '홍길동', '홍길동', 'USER', NOW());
SET @member_id = LAST_INSERT_ID();

-- ============================================================
-- cheering (오늘의 응원 참여 기록. 보통 API로 생성되므로 수동 삽입은 테스트용)
-- ============================================================
INSERT INTO cheering (member_id, item_id, cheering_date, created_at)
VALUES (@member_id, @cheering_item_id, CURDATE(), NOW());

-- ============================================================
-- short_link
-- ============================================================
INSERT INTO short_link (short_key, original_url, title, created_at)
VALUES ('abc123', 'melonapp://album/12345', '멜론 앱으로 이동', NOW());
SET @short_link_id = LAST_INSERT_ID();

-- ============================================================
-- link_click (보통 리다이렉트될 때 자동 생성되므로 수동 삽입은 테스트용)
-- ============================================================
INSERT INTO link_click (short_link_id, clicked_at)
VALUES (@short_link_id, NOW());
