package zerobase.bud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.domain.CommitHistory;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.exception.BudException;
import zerobase.bud.repository.CommitHistoryRepository;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static zerobase.bud.type.ErrorCode.FAILED_CONNECT_GITHUB;
import static zerobase.bud.type.ErrorCode.FAILED_GET_COMMIT_INFO;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubApi {

    private final CommitHistoryRepository commitHistoryRepository;

    private GitHub github;

    @Transactional
    public String saveCommitInfoFromLastCommitDate(
            GithubInfo githubInfo
            , LocalDate lastCommitDate
    ) {

        log.info(
                "start saveCommitInfoFromLastCommitDate... " + LocalDateTime.now()
        );

        connectGithub(githubInfo.getAccessToken());

        log.info("success to connect");

        Map<LocalDate, Long> commitDateCountMap = new HashMap<>();

        getCommitInfoFromGithub(githubInfo.getUsername(), commitDateCountMap,
                lastCommitDate);

        saveCommitHistory(githubInfo, commitDateCountMap);

        log.info("complete saveCommitInfoFromLastCommitDate... "
                + LocalDateTime.now());

        return githubInfo.getEmail();
    }

    private void saveCommitHistory(
            GithubInfo githubInfo
            , Map<LocalDate, Long> commitDateCountMap
    ) {
        List<Entry<LocalDate, Long>> commitDateCountList =
                new ArrayList<>(commitDateCountMap.entrySet());

        if (commitDateCountMap.size() > 1) {
            commitDateCountList = commitDateCountMap.entrySet()
                    .stream().sorted(Entry.comparingByKey())
                    .collect(Collectors.toList());
        }

        for (Entry<LocalDate, Long> entry : commitDateCountList) {
            Optional<CommitHistory> byCommitDate =
                    commitHistoryRepository.findByGithubInfoIdAndCommitDate(
                            githubInfo.getId()
                            , entry.getKey()
                    );

            if (byCommitDate.isPresent()) {
                byCommitDate.get().setCommitCount(entry.getValue());
            } else {
                saveCommitHistory(githubInfo, entry);
            }
        }
    }

    private void getCommitInfoFromGithub(
            String username
            , Map<LocalDate, Long> commitDateCountMap
            , LocalDate lastCommitDate
    ) {
        try {
            GHUser user = github.getUser(username);
            log.info(
                    username + " 님의 CommitLog를 가져옵니다... " + LocalDateTime.now()
            );

            List<GHRepository> repositories = user.listRepositories().toList();

            for (GHRepository repository : repositories) {
                log.info("started Repository... " + repository.getFullName());
                GHCommitQueryBuilder commitQueryBuilder = repository.queryCommits();

                commitQueryBuilder.author(username);
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
    }

    private void connectGithub(String accessToken) {
        try {
            github = new GitHubBuilder().withOAuthToken(accessToken).build();
            github.checkApiUrlValidity();
        } catch (IOException e) {
            log.error("IOException is occurred FAILED_CONNECT_GITHUB ", e);
            throw new BudException(FAILED_CONNECT_GITHUB);
        }
    }

    private void saveCommitHistory(
            GithubInfo githubInfo
            , Entry<LocalDate, Long> entry
    ) {
        commitHistoryRepository.save(CommitHistory.builder()
                .githubInfo(githubInfo)
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
}
