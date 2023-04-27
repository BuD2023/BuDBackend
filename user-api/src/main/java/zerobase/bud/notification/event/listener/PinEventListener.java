package zerobase.bud.notification.event.listener;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.event.listener.common.GetNotificationInfo;
import zerobase.bud.notification.event.pin.CommentPinEvent;
import zerobase.bud.notification.event.pin.QnaAnswerCommentPinEvent;
import zerobase.bud.notification.event.pin.QnaAnswerPinEvent;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;

@Slf4j
@Component
@Async("asyncNotification")
@RequiredArgsConstructor
public class PinEventListener {

    private final FcmApi fcmApi;

    private final GetNotificationInfo getNotificationInfo;

    @EventListener
    public void handleQnaAnswerPinEvent(QnaAnswerPinEvent qnaAnswerPinEvent){
        try {
            Member sender = qnaAnswerPinEvent.getMember();
            QnaAnswer qnaAnswer = qnaAnswerPinEvent.getQnaAnswer();
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.QNA
                        , qnaAnswer.getPost().getId()
                        , NotificationDetailType.ANSWER_PIN
                    )
                );
            }

        } catch (Exception e) {
            log.error("QnaAnswerPin 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleQnaAnswerCommentPinEvent(
        QnaAnswerCommentPinEvent qnaAnswerCommentPinEvent){
        try {
            Member sender = qnaAnswerCommentPinEvent.getMember();
            QnaAnswerComment qnaAnswerComment = qnaAnswerCommentPinEvent.getQnaAnswerComment();
            Member receiver = qnaAnswerComment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                Post post = qnaAnswerComment.getQnaAnswer().getPost();
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.QNA
                        , post.getId()
                        , NotificationDetailType.ANSWER_COMMENT_PIN
                    )
                );
            }

        } catch (Exception e) {
            log.error(
                "QnaAnswerCommentPin 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleCommentPinEvent(CommentPinEvent commentPinEvent){
        try {
            Member sender = commentPinEvent.getMember();
            Comment comment = commentPinEvent.getComment();
            Member receiver = comment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(comment.getPost().getPostType().name())
                        , comment.getPost().getId()
                        , NotificationDetailType.COMMENT_PIN
                    )
                );
            }

        } catch (Exception e) {
            log.error(
                "CommentPin 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }
}
