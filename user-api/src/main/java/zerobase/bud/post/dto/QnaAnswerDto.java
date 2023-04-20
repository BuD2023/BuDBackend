package zerobase.bud.post.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
public class QnaAnswerDto {
    private Long id;
    private Member member;
    private String content;
    private long commentCount;
    private long likeCount;
    private QnaAnswerStatus qnaAnswerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long pinId;
    private boolean isLike;
    private boolean isFollow;

    @QueryProjection
    public QnaAnswerDto(Long id, Member member, String content,
                        long commentCount, long likeCount,
                        QnaAnswerStatus qnaAnswerStatus,
                        LocalDateTime createdAt, LocalDateTime updatedAt,
                        Long pinId, boolean isLike, boolean isFollow) {
        this.id = id;
        this.member = member;
        this.content = content;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.qnaAnswerStatus = qnaAnswerStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pinId = pinId;
        this.isLike = isLike;
        this.isFollow = isFollow;
    }
}
