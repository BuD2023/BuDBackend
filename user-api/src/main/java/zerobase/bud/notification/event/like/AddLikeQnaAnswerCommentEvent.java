package zerobase.bud.notification.event.like;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class AddLikeQnaAnswerCommentEvent {
    private final Member member;
    private final QnaAnswerComment qnaAnswerComment;
    private final Long postId;
}
