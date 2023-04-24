package zerobase.bud.notification.event.listner;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.comment.domain.QnaAnswerComment;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.event.AddLikeCommentEvent;
import zerobase.bud.notification.event.AddLikePostEvent;
import zerobase.bud.notification.event.AddLikeQnaAnswerCommentEvent;
import zerobase.bud.notification.event.AddLikeQnaAnswerEvent;
import zerobase.bud.notification.event.CommentPinEvent;
import zerobase.bud.notification.event.CreatePostEvent;
import zerobase.bud.notification.event.CreateQnaAnswerEvent;
import zerobase.bud.notification.event.FollowEvent;
import zerobase.bud.notification.event.QnaAnswerCommentPinEvent;
import zerobase.bud.notification.event.QnaAnswerPinEvent;
import zerobase.bud.notification.repository.NotificationInfoRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.repository.FollowRepository;

@Slf4j
@Component
@Async("asyncNotification")
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationInfoRepository notificationInfoRepository;

    private final FcmApi fcmApi;

    private final FollowRepository followRepository;

    @EventListener
    public void handleCreatePostEvent(CreatePostEvent createPostEvent){
        try {
            Member sender = createPostEvent.getMember();
            Post post = createPostEvent.getPost();
            List<Member> followerList = followRepository.findAllByTargetId(sender.getId())
                .stream()
                .map(Follow::getMember)
                .collect(Collectors.toList());

            log.info(
                "follower 가 " + followerList.size() + " 명 입니다. 알림 전송을 시작합니다.");

            for (Member receiver : followerList) {
                Optional<NotificationInfo> optionalNotificationInfo =
                    notificationInfoRepository.findByMemberId(receiver.getId());

                if (optionalNotificationInfo.isEmpty()) {
                    log.error(receiver.getNickname() + "님의 알림정보가 없습니다.");
                    continue;
                }

                NotificationInfo notificationInfo = optionalNotificationInfo.get();

                if (!Objects.equals(receiver.getId(), sender.getId())
                    && notificationInfo.isFollowPushAvailable()) {
                    fcmApi.sendNotification(
                        NotificationDto.of(
                            notificationInfo.getFcmToken()
                            , receiver
                            , sender
                            , NotificationType.FOLLOW
                            , PageType.valueOf(post.getPostType().name())
                            , post.getId()
                            , NotificationDetailType.NEW_POST
                        )
                    );
                }
            }
        } catch (Exception e) {
            log.error("CreatePost 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleAddLikePostEvent(AddLikePostEvent addLikePostEvent){
        try {
            Member sender = addLikePostEvent.getMember();
            Post post = addLikePostEvent.getPost();
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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
    public void handleCreateQnaAnswerEvent(CreateQnaAnswerEvent createQnaAnswerEvent){
        try {
            Member sender = createQnaAnswerEvent.getMember();
            Post post = createQnaAnswerEvent.getPost();
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.QNA
                        , post.getId()
                        , NotificationDetailType.ANSWER
                    )
                );
            }

        } catch (Exception e) {
            log.error("CreateQnaAnswer 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleQnaAnswerPinEvent(QnaAnswerPinEvent qnaAnswerPinEvent){
        try {
            Member sender = qnaAnswerPinEvent.getMember();
            QnaAnswer qnaAnswer = qnaAnswerPinEvent.getQnaAnswer();
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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
    public void handleAddLikeQnaAnswerEvent(
        AddLikeQnaAnswerEvent addLikeQnaAnswerEvent){
        try {
            Member sender = addLikeQnaAnswerEvent.getMember();
            QnaAnswer qnaAnswer = addLikeQnaAnswerEvent.getQnaAnswer();
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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
    public void handleFollowEvent(FollowEvent followEvent){
        try {
            Member sender = followEvent.getMember();
            Member receiver = followEvent.getTargetMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isFollowPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.FOLLOW
                        , PageType.OTHER_PROFILE
                        , sender.getId()
                        , NotificationDetailType.FOLLOWED
                    )
                );
            }

        } catch (Exception e) {
            log.error("Follow 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleAddLikeCommentEvent(AddLikeCommentEvent addLikeCommentEvent){
        try {
            Member sender = addLikeCommentEvent.getMember();
            Comment comment = addLikeCommentEvent.getComment();
            Member receiver = comment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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
    public void handleCommentPinEvent(CommentPinEvent commentPinEvent){
        try {
            Member sender = commentPinEvent.getMember();
            Comment comment = commentPinEvent.getComment();
            Member receiver = comment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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

    @EventListener
    public void handleAddLikeQnaAnswerCommentEvent(
        AddLikeQnaAnswerCommentEvent addLikeQnaAnswerCommentEvent){
        try {
            Member sender = addLikeQnaAnswerCommentEvent.getMember();
            QnaAnswerComment qnaAnswerComment = addLikeQnaAnswerCommentEvent.getQnaAnswerComment();
            Member receiver = qnaAnswerComment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

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

    @EventListener
    public void handleQnaAnswerCommentPinEvent(
        QnaAnswerCommentPinEvent qnaAnswerCommentPinEvent){
        try {
            Member sender = qnaAnswerCommentPinEvent.getMember();
            QnaAnswerComment qnaAnswerComment = qnaAnswerCommentPinEvent.getQnaAnswerComment();
            Member receiver = qnaAnswerComment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                Post post = qnaAnswerComment.getQnaAnswer().getPost();
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(post.getPostType().name())
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

    private NotificationInfo getNotificationInfo(Long receiverId) {
        return notificationInfoRepository.findByMemberId(receiverId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));
    }
}
