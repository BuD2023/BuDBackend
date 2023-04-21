package zerobase.bud.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class CommentPinEvent {
    private final Member member;
    private final Comment comment;
}
