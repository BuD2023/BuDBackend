package zerobase.bud.notification.service;

import static zerobase.bud.common.type.ErrorCode.DELETED_NOTIFICATION;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION;
import static zerobase.bud.common.type.ErrorCode.NOT_RECEIVED_NOTIFICATION_MEMBER;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.dto.GetNotifications.Response;
import zerobase.bud.notification.repository.NotificationRepository;
import zerobase.bud.notification.type.NotificationStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Slice<Response> getNotifications(Member member, Pageable pageable) {
        return notificationRepository
            .findAllByReceiverIdAndNotificationStatusNot(
                member.getId()
                , NotificationStatus.DELETED
                , pageable)
            .map(Response::fromEntity);
    }


    @Transactional
    public String updateNotificationStatusRead(String notificationId , Member member) {

        Notification notification = notificationRepository.findByNotificationId(
                notificationId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION));

        validateUpdateNotificationStatus(notification, member);

        notification.updateStatus();

        return notificationId;
    }

    private void validateUpdateNotificationStatus(Notification notification, Member member) {
        if(Objects.equals(NotificationStatus.DELETED, notification.getNotificationStatus())){
            throw new BudException(DELETED_NOTIFICATION);
        }

        if(!Objects.equals(notification.getReceiver().getId(), member.getId())){
            throw new BudException(NOT_RECEIVED_NOTIFICATION_MEMBER);
        }
    }
}
