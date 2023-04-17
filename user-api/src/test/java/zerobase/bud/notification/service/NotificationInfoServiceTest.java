package zerobase.bud.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.SaveNotificationInfo.Request;
import zerobase.bud.notification.repository.NotificationInfoRepository;

@ExtendWith(MockitoExtension.class)
class NotificationInfoServiceTest {

    @Mock
    private NotificationInfoRepository notificationInfoRepository;

    @InjectMocks
    private NotificationInfoService notificationInfoService;

    @Test
    void saveNotificationInfo() {
        //given
        given(notificationInfoRepository.save(any()))
            .willReturn(NotificationInfo.builder()
                .fcmToken("fcmToken")
                .isPostPushAvailable(true)
                .isFollowPushAvailable(true)
                .build());

        ArgumentCaptor<NotificationInfo> captor = ArgumentCaptor.forClass(
            NotificationInfo.class);

        //when
        String fcmToken = notificationInfoService.saveNotificationInfo(
            new Request(
                "fcmToken", true, true
            ), getMember()
        );
        //then
        verify(notificationInfoRepository, times(1)).save(captor.capture());
        assertEquals("fcmToken", captor.getValue().getFcmToken());
        assertTrue(captor.getValue().isPostPushAvailable());
        assertTrue(captor.getValue().isFollowPushAvailable());
        assertEquals("fcmToken", fcmToken);
    }

    private Member getMember() {
        return Member.builder()
            .id(1L)
            .nickname("member")
            .userId("member")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }
}