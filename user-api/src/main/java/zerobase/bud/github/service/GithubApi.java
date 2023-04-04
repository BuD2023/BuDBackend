package zerobase.bud.github.service;

import static zerobase.bud.common.type.ErrorCode.FAILED_CONNECT_GITHUB;
import static zerobase.bud.common.type.ErrorCode.FAILED_GET_COMMIT_INFO;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.io.IOException;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.github.domain.CommitHistory;
import zerobase.bud.github.domain.GithubInfo;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.github.repository.CommitHistoryRepository;
import zerobase.bud.github.repository.GithubInfoRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubApi {

    private final GithubInfoRepository githubInfoRepository;
    private final CommitHistoryRepository commitHistoryRepository;

    private GitHub github;

    public CommitHistoryInfo getCommitInfo(String email) {

        GithubInfo githubInfo = githubInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        List<CommitHistory> commitHistories = commitHistoryRepository
            .findAllByGithubInfoIdOrderByCommitDateDesc(githubInfo.getId());

        long totalCommitCount = 0;
        long todayCommitCount = 0;
        long consecutiveCommitDays = 0;
        long thisWeekCommitCount = 0;

        if (commitHistories.isEmpty()) {
            return CommitHistoryInfo.builder()
                .totalCommitCount(totalCommitCount)
                .todayCommitCount(todayCommitCount)
                .thisWeekCommitCount(thisWeekCommitCount)
                .continuouslyCommitDays(consecutiveCommitDays)
                .commits(new ArrayList<>())
                .build();
        }

        if (LocalDate.now().isEqual(commitHistories.get(0).getCommitDate())) {
            todayCommitCount = commitHistories.get(0).getCommitCount();
            consecutiveCommitDays = commitHistories.get(0)
                .getConsecutiveCommitDays();
        }

        LocalDate firstDayOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        long duration = Period.between(firstDayOfWeek, LocalDate.now())
            .getDays();

        for (int i = 0; i <= duration; i++) {
            thisWeekCommitCount += commitHistories.get(i).getCommitCount();
        }

        List<CommitCountByDate> commits = commitHistories
            .stream()
            .map(CommitCountByDate::from)
            .sorted(Comparator.comparing(CommitCountByDate::getCommitDate))
            .collect(Collectors.toList());

        totalCommitCount = commits.stream()
            .map(CommitCountByDate::getCommitCount)
            .reduce(0L, Long::sum);

        return CommitHistoryInfo.builder()
            .totalCommitCount(totalCommitCount)
            .todayCommitCount(todayCommitCount)
            .thisWeekCommitCount(thisWeekCommitCount)
            .continuouslyCommitDays(consecutiveCommitDays)
            .commits(commits)
            .build();
    }

    @Transactional
    public String saveCommitInfoFromLastCommitDate(
        String email
        , String userName
    ) {

        GithubInfo githubInfo = githubInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        connectGithub(githubInfo.getAccessToken());

        log.info("success to connect");

        Map<LocalDate, Long> commitDateCountMap = new HashMap<>();
        LocalDate lastCommitDate = getLastCommitDate(githubInfo);

        try {

            GHUser user = github.getUser(userName);
            log.info(
                userName + " 님의 CommitLog를 가져옵니다... " + LocalDateTime.now()
            );

            List<GHRepository> repositories = user.listRepositories().toList();

            for (GHRepository repository : repositories) {
                GHCommitQueryBuilder commitQueryBuilder = repository.queryCommits();

                commitQueryBuilder.author(userName);
                commitQueryBuilder.since(Date.valueOf(lastCommitDate));

                List<GHCommit> commits = commitQueryBuilder.list().toList();

                for (GHCommit commit : commits) {

                    Instant instant = commit.getCommitShortInfo()
                        .getCommitDate()
                        .toInstant();

                    LocalDate localDate = instant.atZone(ZoneId.systemDefault())
                        .toLocalDate();

                    commitDateCountMap.put(localDate,
                        commitDateCountMap.getOrDefault(localDate, 0L) + 1);
                }
            }
        } catch (IOException e) {
            log.error("IOException is occurred FAILED_GET_COMMIT_INFO", e);
            throw new BudException(FAILED_GET_COMMIT_INFO);
        }

        List<Entry<LocalDate, Long>> commitDateCountList =
            new ArrayList<>(commitDateCountMap.entrySet());

        if (commitDateCountMap.size() > 1) {
            commitDateCountList = commitDateCountMap.entrySet()
                .stream().sorted(Comparator.comparing(Entry::getKey))
                .collect(
                    Collectors.toList());
        }

        for (Entry<LocalDate, Long> entry : commitDateCountList) {
            Optional<CommitHistory> byCommitDate =
                commitHistoryRepository.findByGithubInfoIdAndCommitDate(
                    githubInfo.getId(),
                    entry.getKey()
                );

            if (byCommitDate.isPresent()) {
                byCommitDate.get().setCommitCount(entry.getValue());
            } else {
                saveCommitHistory(githubInfo, entry);
            }
        }

        log.info("success to get commitInfo... " + LocalDateTime.now());

        return email;
    }

    private void connectGithub(String accessToken) {
        try {
            connectToGithub(accessToken);
        } catch (IOException e) {
            log.error("IOException is occurred FAILED_CONNECT_GITHUB ", e);
            throw new BudException(FAILED_CONNECT_GITHUB);
        }
    }

    private LocalDate getLastCommitDate(GithubInfo githubInfo) {
        return commitHistoryRepository.findFirstByGithubInfoIdOrderByCommitDateDesc(
                githubInfo.getId())
            .stream()
            .map(CommitHistory::getCommitDate)
            .findFirst()
            .orElse(githubInfo.getCreatedAt().toLocalDate());
    }

    private void saveCommitHistory(
        GithubInfo githubInfo
        , Entry<LocalDate, Long> entry
    ) {
        commitHistoryRepository.save(CommitHistory.builder()
            .githubInfoId(githubInfo.getId())
            .memberId(githubInfo.getMemberId())
            .commitDate(entry.getKey())
            .commitCount(entry.getValue())
            .consecutiveCommitDays(
                getConsecutiveCommitDays(
                    githubInfo.getId()
                    , entry.getKey()
                )
            )
            .build());
    }

    private long getConsecutiveCommitDays(
        Long githubInfoId
        , LocalDate commitDate
    ) {
        return commitHistoryRepository.findByGithubInfoIdAndCommitDate(
                githubInfoId
                , commitDate.minusDays(1)
            )
            .map(CommitHistory::getConsecutiveCommitDays)
            .orElse(0L) + 1;
    }

    private void connectToGithub(String token) throws IOException {
        github = new GitHubBuilder().withOAuthToken(token).build();
        github.checkApiUrlValidity();
    }
}
