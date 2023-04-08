package zerobase.bud.github.service;

import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.CommitHistory;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.repository.CommitHistoryRepository;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.service.GithubApi;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubService {

    private final GithubInfoRepository githubInfoRepository;
    private final CommitHistoryRepository commitHistoryRepository;

    private final GithubApi githubApi;


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
                .consecutiveCommitDays(consecutiveCommitDays)
                .commits(new ArrayList<>())
                .build();
        }

        CommitHistory latestCommitHistory = commitHistories.get(0);

        if (LocalDate.now().isEqual(latestCommitHistory.getCommitDate())) {
            todayCommitCount = latestCommitHistory.getCommitCount();
            consecutiveCommitDays = latestCommitHistory
                .getConsecutiveCommitDays();
        }

        thisWeekCommitCount = getThisWeekCommitCount(
            commitHistories, thisWeekCommitCount);

        List<CommitCountByDate> commits = getCommits(commitHistories);

        totalCommitCount = getTotalCommitCount(commits);

        return CommitHistoryInfo.builder()
            .totalCommitCount(totalCommitCount)
            .todayCommitCount(todayCommitCount)
            .thisWeekCommitCount(thisWeekCommitCount)
            .consecutiveCommitDays(consecutiveCommitDays)
            .commits(commits)
            .build();
    }

    public String saveCommitInfoFromLastCommitDate(
        String email
    ) {
        GithubInfo githubInfo = githubInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        return githubApi.saveCommitInfoFromLastCommitDate(
            githubInfo, getLastCommitDate(githubInfo)
        );
    }

    private LocalDate getLastCommitDate(GithubInfo githubInfo) {
        return commitHistoryRepository.findFirstByGithubInfoIdOrderByCommitDateDesc(
                githubInfo.getId())
            .stream()
            .map(CommitHistory::getCommitDate)
            .findFirst()
            .orElse(githubInfo.getCreatedAt().toLocalDate());
    }


    private Long getTotalCommitCount(List<CommitCountByDate> commits) {
        return commits.stream()
            .map(CommitCountByDate::getCommitCount)
            .reduce(0L, Long::sum);
    }

    private List<CommitCountByDate> getCommits(
        List<CommitHistory> commitHistories
    ) {
        return commitHistories
            .stream()
            .map(CommitCountByDate::from)
            .sorted(Comparator.comparing(CommitCountByDate::getCommitDate))
            .collect(Collectors.toList());
    }

    private long getThisWeekCommitCount(
        List<CommitHistory> commitHistories
        , long thisWeekCommitCount
    ) {
        LocalDate firstDayOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        long duration = Math.min(Period.between(firstDayOfWeek, LocalDate.now())
            .getDays(), commitHistories.size() - 1);

        for (int i = 0; i <= duration; i++) {
            thisWeekCommitCount += commitHistories.get(i).getCommitCount();
        }
        return thisWeekCommitCount;
    }
}
