package zerobase.bud.notification.repository;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.type.NotificationStatus;

@Repository
public interface NotificationRepository extends
    JpaRepository<Notification, Long> {

    Slice<Notification> findAllByReceiverId(Long receiverId, Pageable pageable);

    Optional<Notification> findByNotificationId(String notificationId);

    long countByReceiverIdAndNotificationStatus(Long receiverId, NotificationStatus unread);
}
