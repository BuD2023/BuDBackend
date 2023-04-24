package zerobase.bud.post.dto;

import lombok.*;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.QnaAnswerStatus;

import java.time.LocalDateTime;

public class SearchQnaAnswer {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {
        private Long id;
        private Member member;
        private String content;
        private long commentCount;
        private long likeCount;
        private QnaAnswerStatus qnaAnswerStatus;
        private boolean isQnaAnswerPin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean isLike;
        private boolean isFollow;

        public static Response from(QnaAnswerDto qnaAnswerDto) {
            return Response.builder()
                    .id(qnaAnswerDto.getId())
                    .member(qnaAnswerDto.getMember())
                    .content(qnaAnswerDto.getContent())
                    .commentCount(qnaAnswerDto.getCommentCount())
                    .likeCount(qnaAnswerDto.getLikeCount())
                    .qnaAnswerStatus(qnaAnswerDto.getQnaAnswerStatus())
                    .isQnaAnswerPin(qnaAnswerDto.getPinId() != null)
                    .createdAt(qnaAnswerDto.getCreatedAt())
                    .updatedAt(qnaAnswerDto.getUpdatedAt())
                    .isLike(qnaAnswerDto.isLike())
                    .isFollow(qnaAnswerDto.isFollow())
                    .build();
        }
    }
}
