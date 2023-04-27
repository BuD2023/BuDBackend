package zerobase.bud.notification.event.create;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class CreateRecommentEvent {

    private final Member member;
    private final Comment parentComment;

}
