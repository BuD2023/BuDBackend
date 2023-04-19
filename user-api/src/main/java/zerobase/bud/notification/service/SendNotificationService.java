package zerobase.bud.notification.service;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public void sendCreatePostNotification(Member member, Post post) {
        try {
            List<Member> followerList = followRepository.findByTarget(member)
                .map(Follow::getMember)
                .collect(Collectors.toList());

            log.info("follower 가 " + followerList.size() + " 명 입니다. 알림 전송을 시작합니다.");

            for (Member receiver : followerList) {
                NotificationInfo notificationInfo = notificationInfoRepository
                    .findByMemberId(receiver.getId())
                    .orElseThrow(
                        () -> new BudException(NOT_FOUND_NOTIFICATION_INFO));

                //테스트를 위해 우선 주석처리 해두었음. 수신자와 송신자가 같으면 알람발생 x
                //post 알람이 꺼져있는 수신자의 경우 알람발생 x
    //        if (!Objects.equals(receiver.getId(), member.getId())
    //            && notificationInfo.isPostPushAvailable()) {
    //            sendNotification(notificationInfo.getFcmToken(), receiver, member,
    //                post);
    //        }

                if (notificationInfo.isFollowPushAvailable()) {
                    fcmApi.sendNotificationByToken(
                        NotificationDto.of(
                            notificationInfo.getFcmToken()
                            , receiver
                            , member
                            , NotificationType.FOLLOW
                            , PageType.valueOf(post.getPostType().name())
                            , post.getId()
                            , NotificationDetailType.NEW_POST
                        )
                    );
                }
            }
        } catch (Exception e) {
            log.error("sendCreatePostNotification 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(),e);
        }
    }

    public void sendCreateQnaAnswerNotification(Member member, Post post) {
        try {
            Member receiver = post.getMember();

            NotificationInfo notificationInfo = notificationInfoRepository
                .findByMemberId(receiver.getId())
                .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));

            //테스트를 위해 우선 주석처리 해두었음. 수신자와 송신자가 같으면 알람발생 x
            //post 알람이 꺼져있는 수신자의 경우 알람발생 x
//        if (!Objects.equals(receiver.getId(), member.getId())
//            && notificationInfo.isPostPushAvailable()) {
//            sendNotification(notificationInfo.getFcmToken(), receiver, member,
//                post);
//        }

            if (notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , member
                        , NotificationType.POST
                        , PageType.QNA
                        , post.getId()
                        , NotificationDetailType.ANSWER
                    )
                );
            }
        } catch (Exception e) {
            log.error("sendCreateQnaAnswerNotification 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(),e);
        }
    }

    public void sendQnaAnswerPinNotification(
        Member member, QnaAnswer qnaAnswer
    ) {
        try {
            Member receiver = qnaAnswer.getMember();

            NotificationInfo notificationInfo = notificationInfoRepository
                .findByMemberId(receiver.getId())
                .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));

            //테스트를 위해 우선 주석처리 해두었음. 수신자와 송신자가 같으면 알람발생 x
            //post 알람이 꺼져있는 수신자의 경우 알람발생 x
//        if (!Objects.equals(receiver.getId(), member.getId())
//            && notificationInfo.isPostPushAvailable()) {
//            sendNotification(notificationInfo.getFcmToken(), receiver, member,
//                post);
//        }

            if (notificationInfo.isPostPushAvailable()) {
                fcmApi.sendNotificationByToken(
                    NotificationDto.of(
                        notificationInfo.getFcmToken()
                        , receiver
                        , member
                        , NotificationType.POST
                        , PageType.QNA
                        , qnaAnswer.getPost().getId()
                        , NotificationDetailType.PIN
                    )
                );
            }
        } catch (Exception e) {
            log.error("sendQnaAnswerPinNotification 알림을 보내는 중 오류가 발생했습니다. {}", e.getMessage(),e);
        }
    }
}
