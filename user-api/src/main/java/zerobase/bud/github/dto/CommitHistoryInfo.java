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

    Long totalCommitCount;
    Long todayCommitCount;
    Long thisWeekCommitCount;
    Long continuouslyCommitDays;
    List<CommitCountByDate> commits;

}
