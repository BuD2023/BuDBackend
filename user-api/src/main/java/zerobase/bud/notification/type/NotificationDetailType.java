package zerobase.bud.notification.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationDetailType {
    FOLLOWED("다른 사용자가 사용자님을 팔로우하였습니다."),
    NEW_POST("팔로우한 사용자가 새로운 게시물을 작성했습니다."),
    POST_COMMENT("사용자님의 게시글에 댓글이 달렸습니다."),
    POST_RE_COMMENT("사용자님의 게시글 댓글에 댓글이 달렸습니다."),
    ANSWER_COMMENT("사용자님의 답변에 댓글이 달렸습니다."),
    ANSWER_RE_COMMENT("사용자님의 답변 댓글에 댓글이 달렸습니다."),
    ANSWER("사용자님의 게시글에 답변이 달렸습니다."),
    ADD_LIKE_POST("사용자님의 게시글에 좋아요가 눌렸습니다"),
    ADD_LIKE_COMMENT("사용자님의 댓글에 좋아요가 눌렸습니다"),
    ADD_LIKE_ANSWER("사용자님의 답변에 좋아요가 눌렸습니다"),
    ADD_LIKE_ANSWER_COMMENT("사용자님의 답변 댓글에 좋아요가 눌렸습니다"),
    COMMENT_PIN("사용자님의 댓글이 핀 되었습니다"),
    ANSWER_PIN("사용자님의 답변이 핀 되었습니다"),
    ANSWER_COMMENT_PIN("사용자님의 답변 댓글이 핀 되었습니다");

    private final String message;
}
