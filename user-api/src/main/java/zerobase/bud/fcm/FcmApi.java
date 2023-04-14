package zerobase.bud.fcm;

import static zerobase.bud.common.type.ErrorCode.FIREBASE_SEND_MESSAGE_FAILED;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_TOKEN;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.repository.NotificationRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class FcmApi {

    private final NotificationRepository notificationRepository;

    //임시로 fcmToken 받도록 함.
    //이 토큰의 주인이 알림을 받을 사람을 의미한다.
    @Value("${fcm.temp.token}")
    private String fcmTempToken;

    public void sendNotificationByToken(NotificationDto dto) {
//        String token = dto.getSender().getFCMToken();
        String token = fcmTempToken;
        if (Objects.nonNull(token)) {

            WebpushConfig webpushConfig = WebpushConfig.builder()
                .setNotification(
                    new WebpushNotification(dto.getNotificationType().name()
                        , dto.getNotificationDetailType().getMessage()))
                .build();

            // 메시지 만들기
            Message message = Message.builder()
                .setToken(token)
                .setWebpushConfig(webpushConfig)
                .putData("senderId", dto.getSender().getId().toString())
                .putData("senderNickname", dto.getSender().getNickname())
                .putData("notificationType", dto.getNotificationType().name())
                .putData("pageType", dto.getPageType().name())
                .putData("pageId", dto.getPageId().toString())
                .putData("notificationDetailType",
                    dto.getNotificationDetailType().name())
                .putData("notifiedAt", dto.getNotifiedAt().toString())
                .build();

            // 요청에 대한 응답을 받을 response
            try {
                // 알림 발송
                String response = FirebaseMessaging.getInstance().send(message);
                log.info(response);

                notificationRepository.save(Notification.from(dto));
            } catch (FirebaseMessagingException e) {
                log.error(
                    "cannot send to memberList push message. error info : {}",
                    e.getMessage());
                throw new BudException(FIREBASE_SEND_MESSAGE_FAILED);
            }
        } else {
            throw new BudException(NOT_FOUND_TOKEN);
        }
    }
}
