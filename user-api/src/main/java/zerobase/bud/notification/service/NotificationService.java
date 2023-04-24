package zerobase.bud.notification.service;

import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION;
import static zerobase.bud.common.type.ErrorCode.NOT_RECEIVED_NOTIFICATION_MEMBER;
import static zerobase.bud.notification.type.NotificationStatus.UNREAD;

import java.util.Map;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final static String UNREAD_COUNT = "unreadCount";


    public Slice<Response> getNotifications(Member member, Pageable pageable) {
        return notificationRepository
            .findAllByReceiverId(member.getId(), pageable)
            .map(Response::fromEntity);
    }

    public Map<String, Long> getUnreadNotificationCount(Member member) {
        return Map.of( UNREAD_COUNT
            , notificationRepository.countByReceiverIdAndNotificationStatus(member.getId(), UNREAD));
    }


    @Transactional
    public String updateNotificationStatusRead(String notificationId,
        Member member) {

        Notification notification = notificationRepository.findByNotificationId(
                notificationId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION));

        if (!Objects.equals(notification.getReceiver().getId(), member.getId())) {
            throw new BudException(NOT_RECEIVED_NOTIFICATION_MEMBER);
        }

        notification.updateStatus();

        return notificationId;
    }

    public String deleteNotification(String notificationId, Member member) {

        Notification notification = notificationRepository.findByNotificationId(
                notificationId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION));

        if (!Objects.equals(notification.getReceiver().getId(), member.getId())) {
            throw new BudException(NOT_RECEIVED_NOTIFICATION_MEMBER);
        }

        notificationRepository.deleteById(notification.getId());

        return notificationId;
    }
}
