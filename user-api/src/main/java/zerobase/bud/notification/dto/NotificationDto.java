package zerobase.bud.notification.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDto {

    private List<String> tokens;

    private Member sender;

    private NotificationType notificationType;

    private PageType pageType;

    private Long pageId;

    private NotificationDetailType notificationDetailType;

    private NotificationStatus notificationStatus;

    private LocalDateTime notifiedAt;
}
