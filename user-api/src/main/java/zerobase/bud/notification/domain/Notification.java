package zerobase.bud.notification.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import zerobase.bud.domain.BaseEntity;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member sender;

    @Column(unique = true)
    private String notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private PageType pageType;

    private Long pageId;

    @Enumerated(EnumType.STRING)
    private NotificationDetailType notificationDetailType;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    private LocalDateTime notifiedAt;

    public static Notification of(String notificationId, NotificationDto dto) {
        return Notification.builder()
            .sender(dto.getSender())
            .notificationId(notificationId)
            .notificationType(dto.getNotificationType())
            .pageType(dto.getPageType())
            .pageId(dto.getPageId())
            .notificationDetailType(dto.getNotificationDetailType())
            .notificationStatus(dto.getNotificationStatus())
            .notifiedAt(dto.getNotifiedAt())
            .build();
    }
}
