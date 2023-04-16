package zerobase.bud.github.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.domain.CommitHistory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitCountByDate {
    private LocalDate commitDate;
    private long commitCount;

    public static CommitCountByDate from(CommitHistory commitHistory){
        return CommitCountByDate.builder()
            .commitDate(commitHistory.getCommitDate())
            .commitCount(commitHistory.getCommitCount())
            .build();
    }
}
