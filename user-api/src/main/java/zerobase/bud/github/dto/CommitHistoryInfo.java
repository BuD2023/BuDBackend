package zerobase.bud.github.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.domain.Level;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitHistoryInfo {

    private String nickName;
    private String levelCode;
    private String imagePath;
    private long remainCommitCountNextLevel;
    private long totalCommitCount;
    private long todayCommitCount;
    private long thisWeekCommitCount;
    private long consecutiveCommitDays;
    @Builder.Default
    private List<CommitCountByDate> commits = new ArrayList<>();

    public static CommitHistoryInfo of(
        String nickname, Level level
    ) {
        return CommitHistoryInfo.builder()
            .nickName(nickname)
            .levelCode(level.getLevelCode())
            .imagePath(level.getImagePath())
            .remainCommitCountNextLevel(level.getNextLevelStartCommitCount())
            .build();
    }

    public static CommitHistoryInfo of(
        String nickname
        , String levelCode
        , String imagePath
        , long nextLevelStartCommitCount
        , long totalCommitCount
        , long todayCommitCount
        , long thisWeekCommitCount
        , long consecutiveCommitDays
        , List<CommitCountByDate> commits
    ) {
        return CommitHistoryInfo.builder()
            .nickName(nickname)
            .levelCode(levelCode)
            .imagePath(imagePath)
            .remainCommitCountNextLevel(nextLevelStartCommitCount)
            .totalCommitCount(totalCommitCount)
            .todayCommitCount(todayCommitCount)
            .thisWeekCommitCount(thisWeekCommitCount)
            .consecutiveCommitDays(consecutiveCommitDays)
            .commits(commits)
            .build();
    }

}
