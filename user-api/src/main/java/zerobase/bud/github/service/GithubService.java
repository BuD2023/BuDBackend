package zerobase.bud.github.service;

import static zerobase.bud.common.type.ErrorCode.INVALID_INITIAL_VALUE;
import static zerobase.bud.common.type.ErrorCode.INVALID_TOTAL_COMMIT_COUNT;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_GITHUB_USER_ID;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;
import static zerobase.bud.common.util.Constants.MAXIMUM_LEVEL_CODE;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.CommitHistory;
import zerobase.bud.domain.GithubInfo;
import zerobase.bud.domain.Level;
import zerobase.bud.domain.Member;
import zerobase.bud.github.dto.CommitCountByDate;
import zerobase.bud.github.dto.CommitHistoryInfo;
import zerobase.bud.repository.CommitHistoryRepository;
import zerobase.bud.repository.GithubInfoRepository;
import zerobase.bud.repository.LevelRepository;
import zerobase.bud.repository.MemberRepository;
import zerobase.bud.service.GithubApi;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubService {

    private final MemberRepository memberRepository;

    private final LevelRepository levelRepository;

    private final GithubInfoRepository githubInfoRepository;

    private final CommitHistoryRepository commitHistoryRepository;

    private final GithubApi githubApi;

    private static final int WEEKS_FOR_COMMIT_HISTORY = 16;


    @Transactional
    public CommitHistoryInfo getCommitInfo(String userId) {

        GithubInfo githubInfo = githubInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_GITHUB_USER_ID));

        Member member = memberRepository.findByUserId(userId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        List<CommitHistory> commitHistories = commitHistoryRepository
            .findAllByGithubInfoIdAndCommitDateBetweenOrderByCommitDateDesc(
                githubInfo.getId()
                , LocalDate.now().minusWeeks(WEEKS_FOR_COMMIT_HISTORY)
                , LocalDate.now()
            );

        if (commitHistories.isEmpty()) {
            Level level = levelRepository.findByLevelStartCommitCount(0)
                .orElseThrow(() -> new BudException(INVALID_INITIAL_VALUE));

            return CommitHistoryInfo.builder()
                .nickName(member.getNickname())
                .levelCode(level.getLevelCode())
                .remainCommitCountNextLevel(
                    level.getNextLevelStartCommitCount())
                .todayCommitCount(0)
                .thisWeekCommitCount(0)
                .consecutiveCommitDays(0)
                .commits(new ArrayList<>())
                .build();
        }

        long totalCommitCount;
        long remainCommitCountNextLevel;
        long todayCommitCount = 0;
        long consecutiveCommitDays = 0;
        long thisWeekCommitCount;

        CommitHistory latestCommitHistory = commitHistories.get(0);

        if (LocalDate.now().isEqual(latestCommitHistory.getCommitDate())) {
            todayCommitCount = latestCommitHistory.getCommitCount();
            consecutiveCommitDays = latestCommitHistory
                .getConsecutiveCommitDays();
        }

        thisWeekCommitCount = getThisWeekCommitCount(commitHistories);

        List<CommitCountByDate> commits = getCommits(commitHistories);

        totalCommitCount = getTotalCommitCount(member, commits);
        Level level = saveAndGetLevel(member, totalCommitCount);
        remainCommitCountNextLevel =
            level.getNextLevelStartCommitCount() - totalCommitCount;

        return CommitHistoryInfo.builder()
            .nickName(member.getNickname())
            .levelCode(level.getLevelCode())
            .remainCommitCountNextLevel(remainCommitCountNextLevel)
            .todayCommitCount(todayCommitCount)
            .thisWeekCommitCount(thisWeekCommitCount)
            .consecutiveCommitDays(consecutiveCommitDays)
            .commits(commits)
            .build();
    }

    private Level saveAndGetLevel(Member member, long totalCommitCount) {
        Level level = member.getLevel();
        if (!MAXIMUM_LEVEL_CODE.equals(level.getLevelCode())) {
            level = levelRepository.
                findByLevelStartCommitCountLessThanEqualAndNextLevelStartCommitCountGreaterThan(
                    totalCommitCount, totalCommitCount)
                .orElseThrow(
                    () -> new BudException(INVALID_TOTAL_COMMIT_COUNT));

            member.setLevel(level);
        }

        return level;
    }

    public String saveCommitInfoFromLastCommitDate(
        String userId
    ) {
        GithubInfo githubInfo = githubInfoRepository.findByUserId(userId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_GITHUB_USER_ID));

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
                .orElse(LocalDate.now().minusWeeks(WEEKS_FOR_COMMIT_HISTORY));
    }


    private Long getTotalCommitCount(Member member, List<CommitCountByDate> commits) {
        LocalDate registeredDate = member.getCreatedAt().toLocalDate();

        return commits.stream()
            .filter(x -> x.getCommitDate().isAfter(registeredDate)
                        || x.getCommitDate().isEqual(registeredDate)
                    )
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

    private long getThisWeekCommitCount(List<CommitHistory> commitHistories) {

        LocalDate firstDayOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        long thisWeekCommitCount = 0;

        for (CommitHistory commitHistory : commitHistories) {
            LocalDate commitDate = commitHistory.getCommitDate();
            if (commitDate.isBefore(firstDayOfWeek)) {
                break;
            }
            thisWeekCommitCount += commitHistory.getCommitCount();
        }

        return thisWeekCommitCount;
    }
}
