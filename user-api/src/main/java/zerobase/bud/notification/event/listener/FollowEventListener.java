package zerobase.bud.notification.event.listener;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.event.follow.FollowEvent;
import zerobase.bud.notification.event.listener.common.GetNotificationInfo;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@Slf4j
@Component
@Async("asyncNotification")
@RequiredArgsConstructor
public class FollowEventListener {

    private final FcmApi fcmApi;

    private final GetNotificationInfo getNotificationInfo;

    @EventListener
    public void handleFollowEvent(FollowEvent followEvent){
        try {
            Member sender = followEvent.getMember();
            Member receiver = followEvent.getTargetMember();

            NotificationInfo notificationInfo = getNotificationInfo.from(receiver.getId());

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
}
