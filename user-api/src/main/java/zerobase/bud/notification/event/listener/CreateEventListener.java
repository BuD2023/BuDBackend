package zerobase.bud.notification.event.listener;

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
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.event.create.CreateAnswerCommentEvent;
import zerobase.bud.notification.event.create.CreateAnswerRecommentEvent;
import zerobase.bud.notification.event.create.CreateCommentEvent;
import zerobase.bud.notification.event.create.CreatePostEvent;
import zerobase.bud.notification.event.create.CreateQnaAnswerEvent;
import zerobase.bud.notification.event.create.CreateRecommentEvent;
import zerobase.bud.notification.event.listener.common.GetNotificationInfo;
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
public class CreateEventListener {

    private final NotificationInfoRepository notificationInfoRepository;

    private final FcmApi fcmApi;

    private final FollowRepository followRepository;

    private final GetNotificationInfo getNotificationInfo;

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
    public void handleCreateQnaAnswerEvent(CreateQnaAnswerEvent createQnaAnswerEvent){
        try {
            Member sender = createQnaAnswerEvent.getMember();
            Post post = createQnaAnswerEvent.getPost();
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
    public void handleCreateCommentEvent(CreateCommentEvent createCommentEvent){
        try {
            Member sender = createCommentEvent.getMember();
            Post post = createCommentEvent.getPost();
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
                        , PageType.FEED
                        , post.getId()
                        , NotificationDetailType.POST_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("CreateComment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleCreateRecommentEvent(CreateRecommentEvent createRecommentEvent){
        try {
            Member sender = createRecommentEvent.getMember();
            Comment parentComment = createRecommentEvent.getParentComment();

            Post post = parentComment.getPost();
            Member receiver = parentComment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotification(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.FEED
                        , post.getId()
                        , NotificationDetailType.POST_RE_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("CreateRecomment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleCreateAnswerCommentEvent(CreateAnswerCommentEvent createAnswerCommentEvent){
        try {
            Member sender = createAnswerCommentEvent.getMember();
            QnaAnswer qnaAnswer = createAnswerCommentEvent.getQnaAnswer();
            Post post = qnaAnswer.getPost();
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
                        , post.getId()
                        , NotificationDetailType.ANSWER_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("CreateAnswerComment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleCreateAnswerRecommentEvent(CreateAnswerRecommentEvent createAnswerRecommentEvent){
        try {
            Member sender = createAnswerRecommentEvent.getMember();
            QnaAnswerComment parentAnswerComment =
                createAnswerRecommentEvent.getParentAnswerComment();

            Post post = parentAnswerComment.getQnaAnswer().getPost();
            Member receiver = parentAnswerComment.getMember();

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
                        , post.getId()
                        , NotificationDetailType.ANSWER_RE_COMMENT
                    )
                );
            }

        } catch (Exception e) {
            log.error("CreateAnswerRecomment 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(), e);
        }
    }
}
