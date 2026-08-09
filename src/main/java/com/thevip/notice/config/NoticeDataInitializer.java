package com.thevip.notice.config;

import com.thevip.notice.entity.Notice;
import com.thevip.notice.entity.NoticeMenuType;
import com.thevip.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 공지 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class NoticeDataInitializer implements ApplicationRunner {

    private final NoticeRepository noticeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (noticeRepository.count() > 0) {
            return;
        }

        Notice notice = Notice.of(NoticeMenuType.MUSIC,
                "[수정] 스트리밍 리스트 ver.2로 업데이트 됐어요.",
                "안녕하세요, 음원총공팀입니다.\n\n"
                        + "스트리밍 리스트 일부 구성이 수정되어 ver.2로 업데이트 되었습니다.\n\n"
                        + "기존 리스트를 사용 중이셨던 VIP분들은 최신 리스트로 다시 확인 후 스트리밍에 참여해주세요.");
        notice.addImageUrl("https://example.com/music/notice/1.png");
        noticeRepository.save(notice);
    }
}
