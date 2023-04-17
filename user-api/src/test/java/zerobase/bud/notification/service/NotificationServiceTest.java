package zerobase.bud.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.common.type.ErrorCode.DELETED_NOTIFICATION;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION;
import static zerobase.bud.common.type.ErrorCode.NOT_RECEIVED_NOTIFICATION_MEMBER;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import zerobase.bud.common.exception.BudException;
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
        String notificationId = makeNotificationId();

        given(
            notificationRepository.findAllByReceiverIdAndNotificationStatusNot(
                getReceiver().getId(), NotificationStatus.DELETED,
                Pageable.ofSize(1)
            )).willReturn(
            new SliceImpl<>(
                List.of(getNotification(notificationId))
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

    @Test
    void success_updateNotificationStatusRead() {
        //given 어떤 데이터가 주어졌을 때
        String notificationId = makeNotificationId();
        given(notificationRepository.findByNotificationId(anyString()))
            .willReturn(Optional.ofNullable(getNotification(notificationId)));
        //when 어떤 경우에
        String statusRead = notificationService.updateNotificationStatusRead(
            notificationId,
            getReceiver());
        //then 이런 결과가 나온다.
        assertEquals(notificationId, statusRead);
    }

    @Test
    @DisplayName("NOT_FOUND_NOTIFICATION_updateNotificationStatusRead")
    void NOT_FOUND_NOTIFICATION_updateNotificationStatusRead() {
        //given 어떤 데이터가 주어졌을 때
        String notificationId = makeNotificationId();

        given(notificationRepository.findByNotificationId(anyString()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> notificationService.updateNotificationStatusRead(
                notificationId,
                getReceiver()));

        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_NOTIFICATION, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_FOUND_NOTIFICATION_updateNotificationStatusRead")
    void DELETED_NOTIFICATION_updateNotificationStatusRead() {
        //given 어떤 데이터가 주어졌을 때
        String notificationId = makeNotificationId();
        Notification notification = getNotification(notificationId);
        notification.setNotificationStatus(NotificationStatus.DELETED);

        given(notificationRepository.findByNotificationId(anyString()))
            .willReturn(Optional.of(notification));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> notificationService.updateNotificationStatusRead(
                notificationId,
                getReceiver()));

        //then 이런 결과가 나온다.
        assertEquals(DELETED_NOTIFICATION, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_RECEIVED_NOTIFICATION_MEMBER_updateNotificationStatusRead")
    void NOT_RECEIVED_NOTIFICATION_MEMBER_updateNotificationStatusRead() {
        //given 어떤 데이터가 주어졌을 때
        String notificationId = makeNotificationId();
        Notification notification = getNotification(notificationId);
        notification.setReceiver(Member.builder()
            .id(2L)
            .build());
        given(notificationRepository.findByNotificationId(anyString()))
            .willReturn(Optional.of(notification));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> notificationService.updateNotificationStatusRead(
                notificationId,
                getReceiver()));

        //then 이런 결과가 나온다.
        assertEquals(NOT_RECEIVED_NOTIFICATION_MEMBER,
            budException.getErrorCode());
    }

    private static String makeNotificationId() {
        return UUID.randomUUID().toString()
            .replaceAll(REPLACE_EXPRESSION, "")
            .concat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
    }

    private Notification getNotification(String notificationId) {
        return Notification.builder()
            .receiver(getReceiver())
            .sender(getSender())
            .notificationId(notificationId)
            .notificationType(NotificationType.POST)
            .pageType(PageType.QNA)
            .pageId(1L)
            .notificationDetailType(NotificationDetailType.ANSWER)
            .notificationStatus(NotificationStatus.UNREAD)
            .notifiedAt(LocalDateTime.now())
            .build();
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
