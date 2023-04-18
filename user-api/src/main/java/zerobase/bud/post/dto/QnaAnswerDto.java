package zerobase.bud.post.dto;

import lombok.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
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
}
