package zerobase.bud.github.dto;

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

    String nickName;
    String levelCode;
    long remainCommitCountNextLevel;
    long todayCommitCount;
    long thisWeekCommitCount;
    long consecutiveCommitDays;
    List<CommitCountByDate> commits;

}
