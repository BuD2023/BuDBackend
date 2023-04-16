package zerobase.bud.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.dto.GetNotifications.Response;
import zerobase.bud.notification.repository.NotificationRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void success_getNotifications() {
        //given
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        String notificationId = UUID.randomUUID().toString()
            .replaceAll(REPLACE_EXPRESSION, "")
            .concat(now);

        given(
            notificationRepository.findAllByReceiverIdAndNotificationStatusNot(
                getReceiver().getId(), NotificationStatus.DELETED,
                Pageable.ofSize(1)
            )).willReturn(
            new SliceImpl<>(
                List.of(Notification.builder()
                    .receiver(getReceiver())
                    .sender(getSender())
                    .notificationId(notificationId)
                    .notificationType(NotificationType.POST)
                    .pageType(PageType.QNA)
                    .pageId(1L)
                    .notificationDetailType(NotificationDetailType.ANSWER)
                    .notificationStatus(NotificationStatus.UNREAD)
                    .notifiedAt(LocalDateTime.now())
                    .build())
            )
        );
        //when
        Slice<Response> notifications = notificationService.getNotifications(
            getReceiver(), Pageable.ofSize(1));
        //then
        Response notificationResponse = notifications.getContent().get(0);
        assertEquals("sender", notificationResponse.getSenderNickName());

        assertEquals(notificationId, notificationResponse.getNotificationId());

        assertEquals(NotificationType.POST,
            notificationResponse.getNotificationType());
        assertEquals(PageType.QNA, notificationResponse.getPageType());
        assertEquals(1L, notificationResponse.getPageId());
        assertEquals(NotificationDetailType.ANSWER,
            notificationResponse.getNotificationDetailType());
        assertEquals(NotificationStatus.UNREAD,
            notificationResponse.getNotificationStatus());
    }

    private Member getReceiver() {
        return Member.builder()
            .id(1L)
            .nickname("receiver")
            .userId("receiver")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private Member getSender() {
        return Member.builder()
            .id(2L)
            .nickname("sender")
            .userId("sender")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }
}
