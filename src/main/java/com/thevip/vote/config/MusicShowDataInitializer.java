package com.thevip.vote.config;

import com.thevip.vote.entity.MusicShow;
import com.thevip.vote.entity.MusicShowVoteRound;
import com.thevip.vote.repository.MusicShowRepository;
import com.thevip.vote.repository.MusicShowVoteRoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 기동 시 투표 플랜(음악방송 주간 투표 일정) 기본값을 채워둔다.
 * H2 인메모리(create-drop)라 재시작마다 비워지므로 매번 시드가 필요하다.
 */
@Component
@RequiredArgsConstructor
public class MusicShowDataInitializer implements ApplicationRunner {

    private final MusicShowRepository musicShowRepository;
    private final MusicShowVoteRoundRepository musicShowVoteRoundRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (musicShowRepository.count() > 0) {
            return;
        }

        MusicShow show = MusicShow.of("쇼! 음악중심", 0);
        show.updateChannel("MBC");
        show.updateBroadcastTime("매주 토요일 오후 3:55");
        show.updateDescription("매주 토요일 생방송으로 진행되는 MBC 음악방송 순위 투표입니다.");
        musicShowRepository.save(show);

        musicShowVoteRoundRepository.save(
                round(show.getId(), "사전 투표 1", "8/12(화) 10:00 ~ 8/14(목) 18:00", "advance", 0));
        musicShowVoteRoundRepository.save(
                round(show.getId(), "사전 투표 2", "8/14(목) 18:00 ~ 8/15(금) 18:00", "advance", 1));
        musicShowVoteRoundRepository.save(
                round(show.getId(), "생방송 투표", "8/16(토) 12:00 ~ 15:00", "live", 2));
        musicShowVoteRoundRepository.save(
                round(show.getId(), "M PICK 투표", "8/16(토) 12:00 ~ 15:55", "mpick", 3));
    }

    private MusicShowVoteRound round(Long musicShowId, String label, String time, String tone, int sortOrder) {
        MusicShowVoteRound round = MusicShowVoteRound.of(musicShowId, label, time, tone, sortOrder);
        round.addRow("뮤빗", "바로가기");
        round.addRow("뮤니버스", "바로가기");
        return round;
    }
}
