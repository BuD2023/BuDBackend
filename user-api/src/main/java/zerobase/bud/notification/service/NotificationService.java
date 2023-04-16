package zerobase.bud.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import zerobase.bud.domain.Member;
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

}
