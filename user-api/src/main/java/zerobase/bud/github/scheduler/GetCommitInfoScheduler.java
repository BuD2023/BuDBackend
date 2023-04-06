package zerobase.bud.github.scheduler;

import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.repository.GithubInfoRepository;
import zerobase.bud.github.service.GithubApi;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCommitInfoScheduler {

    private final GithubInfoRepository githubInfoRepository;
    private final GithubApi githubApi;

    @Scheduled(cron = "0 5 0 * * *")
    public void getCommitInfoScheduling() {
        log.info("start getCommitInfoScheduling..." + LocalDateTime.now());

        List<GithubInfo> githubInfoList = githubInfoRepository.findAll();

        for (GithubInfo info : githubInfoList) {

            GithubInfo githubInfo = githubInfoRepository.findByEmail(
                    info.getEmail())
                .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

            githubApi.saveCommitInfoFromLastCommitDate(
                info.getEmail()
                , info.getUserName()
                , githubInfo
                , LocalDate.now().minusDays(1)
            );
        }

        log.info("complete getCommitInfoScheduling..." + LocalDateTime.now());
    }
}
