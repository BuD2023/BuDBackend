package zerobase.bud.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.util.TimeUtil;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QnaAnswerRecommentDto {
    private Long commentId;
    private Long parentId;
    private String content;
    private int numberOfLikes;
    private String memberName;
    private String memberProfileUrl;
    private Long memberId;
    private Boolean isReader;
    private Boolean isReaderLiked;
    private String createdAt;

    public static QnaAnswerRecommentDto of(QnaAnswerComment comment) {
        return QnaAnswerRecommentDto.builder()
                .commentId(comment.getId())
                .parentId(comment.getParent().getId())
                .content(comment.getContent())
                .numberOfLikes(comment.getLikeCount())
                .memberName(comment.getMember().getNickname())
                .memberProfileUrl(comment.getMember().getProfileImg())
                .memberId(comment.getMember().getId())
                .createdAt(TimeUtil.caculateTerm(comment.getCreatedAt()))
                .build();
    }
}
