package zerobase.bud.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationInfoDto;
import zerobase.bud.notification.dto.SaveNotificationInfo.Request;
import zerobase.bud.notification.repository.NotificationInfoRepository;

@ExtendWith(MockitoExtension.class)
class NotificationInfoServiceTest {

    @Mock
    private NotificationInfoRepository notificationInfoRepository;

    @InjectMocks
    private NotificationInfoService notificationInfoService;

    @Test
    void success_saveNotificationInfo() {
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

    @Test
    void success_changeNotificationAvailable() {
        //given
        given(notificationInfoRepository.findByMemberId(any()))
            .willReturn(Optional.ofNullable(NotificationInfo.builder()
                .fcmToken("fcmToken")
                .isPostPushAvailable(true)
                .isFollowPushAvailable(true)
                .member(getMember())
                .build()));

        //when
        String nickName = notificationInfoService.changeNotificationAvailable(
            new NotificationInfoDto(
                 true, false
            ), getMember()
        );
        //then
        assertEquals("nickname", nickName);
    }

    @Test
    @DisplayName("NOT_FOUND_NOTIFICATION_INFO_changeNotificationAvailable")
    void NOT_FOUND_NOTIFICATION_INFO_changeNotificationAvailable() {
        //given
        given(notificationInfoRepository.findByMemberId(any()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class
            , () -> notificationInfoService.changeNotificationAvailable(
                new NotificationInfoDto(true, false)
                , getMember()
            ));
        //then
        assertEquals(NOT_FOUND_NOTIFICATION_INFO, budException.getErrorCode());
    }

    private Member getMember() {
        return Member.builder()
            .id(1L)
            .nickname("nickname")
            .userId("member")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }
}