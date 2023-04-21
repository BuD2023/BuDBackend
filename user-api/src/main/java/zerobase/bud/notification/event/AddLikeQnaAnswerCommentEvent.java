package zerobase.bud.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.PostType;

@Getter
@RequiredArgsConstructor
public class AddLikeQnaAnswerCommentEvent {
    private final Member member;
    private final QnaAnswerComment qnaAnswerComment;
    private final PostType postType;
    private final Long postId;
}
