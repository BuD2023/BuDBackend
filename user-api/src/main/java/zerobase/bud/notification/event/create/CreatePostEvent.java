package zerobase.bud.notification.event.create;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.domain.Member;
import zerobase.bud.post.domain.Post;

@Getter
@RequiredArgsConstructor
public class CreatePostEvent {
    private final Member member;
    private final Post post;
}
