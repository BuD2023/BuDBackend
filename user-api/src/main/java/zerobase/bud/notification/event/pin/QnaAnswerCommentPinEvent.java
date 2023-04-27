package zerobase.bud.notification.event.pin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class QnaAnswerCommentPinEvent {
    private final Member member;
    private final QnaAnswerComment qnaAnswerComment;
}
