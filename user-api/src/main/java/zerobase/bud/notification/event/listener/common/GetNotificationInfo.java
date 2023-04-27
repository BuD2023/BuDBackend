package zerobase.bud.notification.event.listener.common;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.repository.NotificationInfoRepository;

@Component
@RequiredArgsConstructor
public class GetNotificationInfo {

    private final NotificationInfoRepository notificationInfoRepository;

    public NotificationInfo from(Long receiverId) {
        return notificationInfoRepository.findByMemberId(receiverId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));
    }


}
