package zerobase.bud.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

public class GetNotifications {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long senderId;

        private String senderNickName;

        private String notificationId;

        private NotificationType notificationType;

        private PageType pageType;

        private Long pageId;

        private NotificationDetailType notificationDetailType;

        private NotificationStatus notificationStatus;

        private LocalDateTime notifiedAt;

        public static Response fromEntity(Notification notification) {
            return Response.builder()
                .senderId(notification.getSender().getId())
                .senderNickName(notification.getSender().getNickname())
                .notificationId(notification.getNotificationId())
                .notificationType(notification.getNotificationType())
                .pageType(notification.getPageType())
                .pageId(notification.getPageId())
                .notificationDetailType(
                    notification.getNotificationDetailType())
                .notificationStatus(notification.getNotificationStatus())
                .notifiedAt(notification.getNotifiedAt())
                .build();
        }
    }

}
