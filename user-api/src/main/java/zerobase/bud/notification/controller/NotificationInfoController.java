package zerobase.bud.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.SaveNotificationInfo;
import zerobase.bud.notification.service.NotificationInfoService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification-info")
public class NotificationInfoController {

    private final NotificationInfoService notificationInfoService;

    @PostMapping
    private ResponseEntity<String> saveNotificationInfo(
        @RequestBody SaveNotificationInfo.Request request,
        @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(
            notificationInfoService.saveNotificationInfo(request, member));
    }

}
