package zerobase.bud.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.SaveNotificationInfo;
import zerobase.bud.notification.repository.NotificationInfoRepository;

@Service
@RequiredArgsConstructor
public class NotificationInfoService {

    private final NotificationInfoRepository notificationInfoRepository;

    public String saveNotificationInfo(SaveNotificationInfo.Request request, Member member) {
        notificationInfoRepository.save(NotificationInfo.of(request, member));
        return request.getFcmToken();
    }
}