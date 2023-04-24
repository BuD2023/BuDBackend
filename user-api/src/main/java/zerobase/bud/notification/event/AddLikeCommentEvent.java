package zerobase.bud.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.domain.Member;
import zerobase.bud.post.type.PostType;

@Getter
@RequiredArgsConstructor
public class AddLikeCommentEvent {
    private final Member member;
    private final Comment comment;
    private final PostType postType;
    private final Long postId;
}
