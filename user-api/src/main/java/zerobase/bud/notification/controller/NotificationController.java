package zerobase.bud.notification.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.service.NotificationService;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmApi fcmApi;

    //테스트용 알람쏴보기
    @PostMapping
    public ResponseEntity<String> sendTestNotification(
        @AuthenticationPrincipal Member member
    ){
        fcmApi.sendNotificationByToken(NotificationDto.builder()
                .sender(member)
                .notificationType(NotificationType.POST)
                .pageType(PageType.FEED)
                .pageId(1L)
                .notificationStatus(NotificationStatus.READ)
                .notificationDetailType(NotificationDetailType.ANSWER)
                .notifiedAt(LocalDateTime.now())
            .build());

        return ResponseEntity.ok("hi");
    }
    //get(알람 리스트 가져오기)
    //알람 상태 변화 (안읽음 -> 읽음)
    //알람 삭제(읽음 -> 삭제 or 안읽음 -> 삭제)

}
