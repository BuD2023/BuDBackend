package zerobase.bud.tasklet;

import static zerobase.bud.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.exception.BudException;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.service.GithubApi;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCommitInfoTasklet {

    private final GithubInfoRepository githubInfoRepository;
    private final GithubApi githubApi;

    public void getCommitInfo() {
        log.info("start getCommitInfo..." + LocalDateTime.now());

        List<GithubInfo> githubInfoList = githubInfoRepository.findAll();

        for (GithubInfo info : githubInfoList) {

            GithubInfo githubInfo = githubInfoRepository.findByEmail(
                    info.getEmail())
                .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

            githubApi.saveCommitInfoFromLastCommitDate(
                githubInfo
                , LocalDate.now().minusDays(1)
            );
        }

        log.info("complete getCommitInfo..." + LocalDateTime.now());
    }
}
