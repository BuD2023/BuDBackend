package zerobase.bud.notification.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.dto.GetNotifications;
import zerobase.bud.notification.service.NotificationService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private final static String SORTING_CRITERIA = "notifiedAt";

    @GetMapping
    public ResponseEntity<Slice<GetNotifications.Response>> getNotifications(
        @AuthenticationPrincipal Member member,
        @PageableDefault(sort = SORTING_CRITERIA , direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(member, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(
        @AuthenticationPrincipal Member member
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(member));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<String> updateNotificationStatusRead(
        @PathVariable String notificationId,
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity
            .ok(notificationService.updateNotificationStatusRead(notificationId, member));
    }

    @PutMapping
    public ResponseEntity<Integer> updateAllNotificationStatusRead(
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity
            .ok(notificationService.updateAllNotificationStatusRead(member));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
        @PathVariable String notificationId,
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity
            .ok(notificationService.deleteNotification(notificationId, member));
    }

    @DeleteMapping
    public ResponseEntity<Integer> deleteAllReadNotification(
        @AuthenticationPrincipal Member member
    ){
        return ResponseEntity.ok(notificationService.deleteAllReadNotifications(member));
    }

}
