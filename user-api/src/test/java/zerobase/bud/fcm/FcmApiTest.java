package zerobase.bud.fcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.notification.type.NotificationStatus.READ;
import static zerobase.bud.notification.type.NotificationType.POST;
import static zerobase.bud.notification.type.PageType.QNA;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import com.google.firebase.messaging.FirebaseMessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.repository.NotificationRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@SpringBootTest
class FcmApiTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Value("${fcm.key.scope}")
    private List<String> fireBaseScope;

    @Value("${fcm.temp.token}")
    private String fcmTempToken;

    @InjectMocks
    private FcmApi fcmApi;


    @Test
    void sendNotificationByToken()
        throws FirebaseMessagingException, IOException {
        String notificationId = makeNotificationId();
        //given
        NotificationDto notificationDto = NotificationDto.builder()
            .tokens(List.of(fcmTempToken))
            .receiver(getReceiver())
            .sender(getSender())
            .notificationType(POST)
            .pageType(QNA)
            .pageId(1L)
            .notificationStatus(READ)
            .notificationDetailType(NotificationDetailType.ANSWER)
            .notifiedAt(LocalDateTime.now())
            .build();

        given(notificationRepository.save(any()))
            .willReturn(zerobase.bud.notification.domain.Notification.builder()
                .receiver(getReceiver())
                .sender(getSender())
                .notificationId(notificationId)
                .notificationType(NotificationType.FOLLOW)
                .pageType(PageType.FEED)
                .pageId(1L)
                .notificationStatus(NotificationStatus.UNREAD)
                .notificationDetailType(NotificationDetailType.ANSWER)
                .notifiedAt(LocalDateTime.now())
                .build());

        ArgumentCaptor<zerobase.bud.notification.domain.Notification>
            captor = ArgumentCaptor.forClass(
            zerobase.bud.notification.domain.Notification.class);

        //when
        // Set up mock Firebase SDK behavior
        fcmApi.sendNotificationByToken(notificationDto);

        //then
        verify(notificationRepository, times(1)).save(captor.capture());
        assertEquals("receiver", captor.getValue().getReceiver().getNickname());
        assertEquals("sender", captor.getValue().getSender().getNickname());
        assertEquals(POST, captor.getValue().getNotificationType());
        assertEquals(QNA, captor.getValue().getPageType());
        assertEquals(1L, captor.getValue().getPageId());
        assertEquals(NotificationDetailType.ANSWER,
            captor.getValue().getNotificationDetailType());
        assertEquals(READ, captor.getValue().getNotificationStatus());
    }

    private static String makeNotificationId() {
        return UUID.randomUUID().toString()
            .replaceAll(REPLACE_EXPRESSION, "")
            .concat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
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