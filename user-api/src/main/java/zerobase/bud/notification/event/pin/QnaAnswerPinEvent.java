package zerobase.bud.notification.event.pin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.QnaAnswer;

@Getter
@RequiredArgsConstructor
public class QnaAnswerPinEvent {
    private final Member member;
    private final QnaAnswer qnaAnswer;
}
