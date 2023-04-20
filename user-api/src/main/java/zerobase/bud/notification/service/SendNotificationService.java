package zerobase.bud.notification.service;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zerobase.bud.comment.domain.Comment;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.repository.NotificationInfoRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.repository.FollowRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendNotificationService {

    private final NotificationInfoRepository notificationInfoRepository;

    private final FcmApi fcmApi;

    private final FollowRepository followRepository;

    public void sendCreatePostNotification(Member sender, Post post) {
        try {
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
                    fcmApi.sendNotificationByToken(
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
            log.error("sendCreatePostNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendCreateQnaAnswerNotification(Member sender, Post post) {
        try {
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
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
            log.error(
                "sendCreateQnaAnswerNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendQnaAnswerPinNotification(Member sender, QnaAnswer qnaAnswer) {
        try {
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.QNA
                        , qnaAnswer.getPost().getId()
                        , NotificationDetailType.PIN
                    )
                );
            }

        } catch (Exception e) {
            log.error("sendQnaAnswerPinNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendFollowedNotification(Member sender, Member receiver) {
        try {
            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isFollowPushAvailable()) {
                fcmApi.sendNotificationByToken(
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
            log.error("sendFollowedNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendAddLikeNotification(Member sender, Post post) {
        try {
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(post.getPostType().name())
                        , post.getId()
                        , NotificationDetailType.LIKE
                    )
                );
            }

        } catch (Exception e) {
            log.error("sendFollowedNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }
    public void sendCommentLikeNotification(Member sender, Comment comment) {
        try {
            Member receiver = comment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            Post post = comment.getPost();

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(post.getPostType().name())
                        , post.getId()
                        , NotificationDetailType.LIKE
                    )
                );
            }

        } catch (Exception e) {
            log.error("sendFollowedNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendCommentPinNotification(Member sender, Comment comment) {
        try {
            Member receiver = comment.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , sender
                        , NotificationType.POST
                        , PageType.valueOf(comment.getPost().getPostType().name())
                        , comment.getPost().getId()
                        , NotificationDetailType.PIN
                    )
                );
            }

        } catch (Exception e) {
            log.error("sendFollowedNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                e.getMessage(), e);
        }
    }

    public void sendQnaAnswerAddLikeNotification(Member sender, QnaAnswer qnaAnswer) {
        try {
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = getNotificationInfo(receiver.getId());

            if (!Objects.equals(receiver.getId(), sender.getId())
                    && notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                        NotificationDto.of(
                                notificationInfo.getFcmToken()
                                , receiver
                                , sender
                                , NotificationType.POST
                                , PageType.QNA
                                , qnaAnswer.getPost().getId()
                                , NotificationDetailType.LIKE
                        )
                );
            }

        } catch (Exception e) {
            log.error("sendQnaAnswerAddLikeNotification 알림을 보내는 중 오류가 발생했습니다. {}",
                    e.getMessage(), e);
        }
    }

    private NotificationInfo getNotificationInfo(Long receiverId) {
        return notificationInfoRepository.findByMemberId(receiverId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));
    }

}
