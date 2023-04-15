package zerobase.bud.github.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitHistoryInfo {

    private String nickName;
    private String levelCode;
    private long remainCommitCountNextLevel;
    private long todayCommitCount;
    private long thisWeekCommitCount;
    private long consecutiveCommitDays;
    @Builder.Default
    private List<CommitCountByDate> commits = new ArrayList<>();

    public static CommitHistoryInfo of(
        String nickname, String levelCode, long nextLevelStartCommitCount
    ) {
        return CommitHistoryInfo.builder()
            .nickName(nickname)
            .levelCode(levelCode)
            .remainCommitCountNextLevel(nextLevelStartCommitCount)
            .build();
    }

    public static CommitHistoryInfo of(
        String nickname
        , String levelCode
        , long nextLevelStartCommitCount
        , long todayCommitCount
        , long thisWeekCommitCount
        , long consecutiveCommitDays
        , List<CommitCountByDate> commits
    ) {
        return CommitHistoryInfo.builder()
            .nickName(nickname)
            .levelCode(levelCode)
            .remainCommitCountNextLevel(nextLevelStartCommitCount)
            .todayCommitCount(todayCommitCount)
            .thisWeekCommitCount(thisWeekCommitCount)
            .consecutiveCommitDays(consecutiveCommitDays)
            .commits(commits)
            .build();
    }

}
