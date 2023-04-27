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
import zerobase.bud.notification.event.like.AddLikeCommentEvent;
import zerobase.bud.notification.event.like.AddLikePostEvent;
import zerobase.bud.notification.event.like.AddLikeQnaAnswerCommentEvent;
import zerobase.bud.notification.event.like.AddLikeQnaAnswerEvent;
import zerobase.bud.notification.event.listener.common.GetNotificationInfo;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;

@Slf4j
@Component
@Async("asyncNotification")
@RequiredArgsConstructor
public class LikeEventListener {

    private final FcmApi fcmApi;

    private final GetNotificationInfo getNotificationInfo;

    @EventListener
    public void handleAddLikePostEvent(AddLikePostEvent addLikePostEvent){
        try {
            Member sender = addLikePostEvent.getMember();
            Post post = addLikePostEvent.getPost();
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(post.getPostType().name())
                        , post.getId()
                        , NotificationDetailType.ADD_LIKE_POST
                    )
                );
            }
        } catch (Exception e) {
            log.error("AddLikePost 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleAddLikeQnaAnswerEvent(
        AddLikeQnaAnswerEvent addLikeQnaAnswerEvent){
        try {
            Member sender = addLikeQnaAnswerEvent.getMember();
            QnaAnswer qnaAnswer = addLikeQnaAnswerEvent.getQnaAnswer();
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
                        , NotificationDetailType.ADD_LIKE_ANSWER
                    )
                );
            }

        } catch (Exception e) {
            log.error("AddLikeQnaAnswer 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleAddLikeCommentEvent(
        AddLikeCommentEvent addLikeCommentEvent){
        try {
            Member sender = addLikeCommentEvent.getMember();
            Comment comment = addLikeCommentEvent.getComment();
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
                        , PageType.valueOf(addLikeCommentEvent.getPostType().name())
                        , addLikeCommentEvent.getPostId()
                        , NotificationDetailType.ADD_LIKE_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("AddLikeComment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleAddLikeQnaAnswerCommentEvent(
        AddLikeQnaAnswerCommentEvent addLikeQnaAnswerCommentEvent){
        try {
            Member sender = addLikeQnaAnswerCommentEvent.getMember();
            QnaAnswerComment qnaAnswerComment = addLikeQnaAnswerCommentEvent.getQnaAnswerComment();
            Member receiver = qnaAnswerComment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(addLikeQnaAnswerCommentEvent.getPostType().name())
                        , addLikeQnaAnswerCommentEvent.getPostId()
                        , NotificationDetailType.ADD_LIKE_ANSWER_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("AddLikeQnaAnswerComment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }
}
