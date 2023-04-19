package zerobase.bud.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.SaveNotificationInfo;
import zerobase.bud.notification.repository.NotificationInfoRepository;

@Service
@RequiredArgsConstructor
public class NotificationInfoService {

    private final NotificationInfoRepository notificationInfoRepository;

    @Transactional
    public String saveNotificationInfo(SaveNotificationInfo.Request request, Member member) {

        notificationInfoRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                    notificationInfo -> notificationInfo.setFcmToken(request.getFcmToken()),
                    () -> notificationInfoRepository.save(NotificationInfo.of(request, member)));

        return request.getFcmToken();
    }
}
