package zerobase.bud.notification.event.create;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class CreateAnswerRecommentEvent {
    private final Member member;
    private final QnaAnswerComment parentAnswerComment;
}
