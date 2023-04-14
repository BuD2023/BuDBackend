package zerobase.bud.notification.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationDetailType {
    FOLLOWED("다른 사용자가 팔로우하였습니다."),
    NEW_POST("팔로우한 사용자가 새로운 게시물을 작성했습니다."),
    COMMENT("게시글에 댓글이 달렸습니다."),
    LIKE("좋아요가 눌렸습니다."),
    ANSWER("답글이 달렸습니다."),
    PIN("고정되었습니다");

    private final String message;
}
