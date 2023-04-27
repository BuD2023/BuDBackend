package zerobase.bud.notification.event.follow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import zerobase.bud.domain.Member;

@Getter
@RequiredArgsConstructor
public class FollowEvent {
    private final Member member;
    private final Member targetMember;
}
