package zerobase.bud.fcm;

import static zerobase.bud.common.type.ErrorCode.FIREBASE_SEND_MESSAGE_FAILED;
import static zerobase.bud.fcm.FcmConstants.*;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void sendNotificationByToken(NotificationDto dto) {
        log.info("start send notification... " + LocalDateTime.now());
        for (String token : dto.getTokens()) {
            WebpushConfig webpushConfig = WebpushConfig.builder()
                .setNotification(new WebpushNotification(
                    dto.getNotificationType().name()
                    , dto.getNotificationDetailType().getMessage()))
                .build();

            String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

            String notificationId = UUID.randomUUID().toString()
                .replaceAll(REPLACE_EXPRESSION, "")
                .concat(now);

            // 메시지 만들기
            Message message = makeMessage(dto, token, webpushConfig,
                notificationId);

            // 요청에 대한 응답을 받을 response
            try {
                // 알림 발송
                String response = FirebaseMessaging.getInstance()
                    .send(message);
                log.info("send message success response: " + response);

                notificationRepository.save(
                    Notification.of(notificationId, dto));
            } catch (FirebaseMessagingException e) {
                log.error(
                    "cannot send to memberList push message. error info : {}",
                    e.getMessage());
                throw new BudException(FIREBASE_SEND_MESSAGE_FAILED);
            }
        }
    }

    private static Message makeMessage(
        NotificationDto dto
        , String token
        , WebpushConfig webpushConfig
        , String notificationId
    ) {
        return Message.builder()
            .setToken(token)
            .setWebpushConfig(webpushConfig)
            .putData(NOTIFICATION_ID, notificationId)
            .putData(SENDER_NICKNAME, dto.getSender().getNickname())
            .putData(NOTIFICATION_TYPE, dto.getNotificationType().name())
            .putData(PAGE_TYPE, dto.getPageType().name())
            .putData(PAGE_ID, dto.getPageId().toString())
            .putData(NOTIFICATION_DETAIL_TYPE,
                dto.getNotificationDetailType().name())
            .putData(NOTIFIED_AT, dto.getNotifiedAt().toString())
            .build();
    }
}
