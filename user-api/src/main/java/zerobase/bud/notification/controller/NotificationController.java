package zerobase.bud.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    //get(알람 리스트 가져오기)
    @GetMapping
    public Slice<GetNotifications.Response> getNotifications(
        @AuthenticationPrincipal Member member,
        @PageableDefault(sort = "notifiedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return notificationService.getNotifications(member, pageable);
    }
    //알람 상태 변화 (안읽음 -> 읽음)
    //알람 삭제(읽음 -> 삭제 or 안읽음 -> 삭제)

}
