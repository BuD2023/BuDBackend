package zerobase.bud.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.Notification;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.repository.NotificationInfoRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;
import zerobase.bud.user.domain.Follow;
import zerobase.bud.user.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock
    private NotificationInfoRepository notificationInfoRepository;

    @Mock
    private FcmApi fcmApi;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private SendNotificationService sendNotificationService;

    @Test
    void success_sendCreatePostNotification() {
        //given
        given(followRepository.findByTarget(any()))
            .willReturn(Stream.of(getFollow()));

        given(notificationInfoRepository.findByMemberId(anyLong()))
            .willReturn(Optional.ofNullable(getNotificationInfo()));
        //when
        sendNotificationService.sendCreatePostNotification(
            getSender(), getPost()
        );
    }

    @Test
    void success_sendCreateQnaAnswerNotification() {
        //given
        given(notificationInfoRepository.findByMemberId(anyLong()))
            .willReturn(Optional.ofNullable(getNotificationInfo()));

        //when
        sendNotificationService.sendCreateQnaAnswerNotification(
            getSender(), getPost()
        );
    }

    @Test
    void success_sendQnaAnswerPinNotification() {
        //given
        given(notificationInfoRepository.findByMemberId(anyLong()))
            .willReturn(Optional.ofNullable(getNotificationInfo()));

        //when
        sendNotificationService.sendQnaAnswerPinNotification(
            getSender(), getQnaAnswer()
        );
    }

    @Test
    void success_sendFollowedNotification() {
        //given
        given(notificationInfoRepository.findByMemberId(anyLong()))
            .willReturn(Optional.ofNullable(getNotificationInfo()));

        //when
        sendNotificationService.sendFollowedNotification(
            getSender(), getReceiver()
        );
    }

    @Test
    void success_sendAddLikeNotification() {
        //given
        given(notificationInfoRepository.findByMemberId(anyLong()))
            .willReturn(Optional.ofNullable(getNotificationInfo()));

        //when
        sendNotificationService.sendAddLikeNotification(
            getSender(), getPost()
        );
    }

    private QnaAnswer getQnaAnswer() {
        return QnaAnswer.builder()
            .id(1L)
            .member(getSender())
            .post(getPost())
            .content("content")
            .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
            .build();
    }

    private Post getPost() {
        return Post.builder()
            .member(getSender())
            .title("title")
            .content("content")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();
    }

    private String makeNotificationId() {
        return UUID.randomUUID().toString()
            .replaceAll(REPLACE_EXPRESSION, "")
            .concat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
    }

    private NotificationInfo getNotificationInfo() {
        return NotificationInfo.builder()
            .fcmToken("fcmToken")
            .isPostPushAvailable(true)
            .isFollowPushAvailable(true)
            .build();
    }

    private Notification getNotification(String notificationId) {
        return Notification.builder()
            .receiver(getReceiver())
            .sender(getSender())
            .notificationId(notificationId)
            .notificationType(NotificationType.POST)
            .pageType(PageType.QNA)
            .pageId(1L)
            .notificationDetailType(NotificationDetailType.ANSWER)
            .notificationStatus(NotificationStatus.UNREAD)
            .notifiedAt(LocalDateTime.now())
            .build();
    }

    private Follow getFollow() {
        return Follow.builder()
            .member(getSender())
            .target(getReceiver())
            .id(2L)
            .build();
    }

    private Member getReceiver() {
        return Member.builder()
            .id(1L)
            .nickname("receiver")
            .userId("receiver")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private Member getSender() {
        return Member.builder()
            .id(2L)
            .nickname("sender")
            .userId("sender")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }
}