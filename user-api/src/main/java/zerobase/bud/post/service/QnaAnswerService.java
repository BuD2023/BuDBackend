package zerobase.bud.post.service;

import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_STATUS;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_TYPE_FOR_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_QNA_ANSWER_STATUS;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_NOTIFICATION_INFO;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_REGISTERED_MEMBER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.fcm.FcmApi;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.dto.NotificationDto;
import zerobase.bud.notification.repository.NotificationInfoRepository;
import zerobase.bud.notification.type.NotificationDetailType;
import zerobase.bud.notification.type.NotificationStatus;
import zerobase.bud.notification.type.NotificationType;
import zerobase.bud.notification.type.PageType;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.QnaAnswerPinRepository;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.type.PostStatus;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;
import zerobase.bud.repository.MemberRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class QnaAnswerService {

    private final FcmApi fcmApi;

    private final MemberRepository memberRepository;

    private final PostRepository postRepository;

    private final QnaAnswerRepository qnaAnswerRepository;

    private final QnaAnswerPinRepository qnaAnswerPinRepository;

    private final NotificationInfoRepository notificationInfoRepository;

    @Transactional
    public String createQnaAnswer(Member member, Request request) {

        Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new BudException(NOT_FOUND_POST));

        validateCreateQnaAnswer(post);

        qnaAnswerRepository.save(QnaAnswer.builder()
            .member(member)
            .post(post)
            .content(request.getContent())
            .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
            .build());

        post.plusCommentCount();

        validateSendNotificationAndSend(member, post);

        return member.getUserId();
    }

    private void validateSendNotificationAndSend(Member member, Post post) {
        Long receiverId = post.getMember().getId();
        NotificationInfo notificationInfo = notificationInfoRepository
            .findByMemberId(receiverId)
            .orElseThrow(() -> new BudException(NOT_FOUND_NOTIFICATION_INFO));

        Member receiver = memberRepository.findById(receiverId)
            .orElseThrow(() -> new BudException(NOT_REGISTERED_MEMBER));

        //테스트를 위해 우선 주석처리 해두었음.
//        if (!Objects.equals(receiver.getId(), member.getId())
//            && notificationInfo.isPostPushAvailable()) {
//            sendNotification(notificationInfo.getFcmToken(), receiver, member,
//                post);
//        }

        if (notificationInfo.isPostPushAvailable()) {
            sendNotification(notificationInfo.getFcmToken(), receiver, member,
                post);
        }
    }

    private void sendNotification(
        String token
        , Member receiver
        , Member sender
        , Post post
    ) {
        fcmApi.sendNotificationByToken(NotificationDto.builder()
            .tokens(List.of(token))
            .receiver(receiver)
            .sender(sender)
            .notificationType(NotificationType.POST)
            .pageType(PageType.QNA)
            .pageId(post.getId())
            .notificationDetailType(NotificationDetailType.ANSWER)
            .notificationStatus(NotificationStatus.UNREAD)
            .notifiedAt(LocalDateTime.now())
            .build());
    }

    @Transactional
    public Long updateQnaAnswer(UpdateQnaAnswer.Request request) {

        QnaAnswer qnaAnswer = qnaAnswerRepository.findById(
                request.getQnaAnswerId())
            .orElseThrow(() -> new BudException(NOT_FOUND_QNA_ANSWER));

        validateUpdateQnaAnswer(qnaAnswer, request);

        qnaAnswer.updateContent(request.getContent());

        return request.getQnaAnswerId();
    }

    private void validateUpdateQnaAnswer(QnaAnswer qnaAnswer,
        UpdateQnaAnswer.Request request) {

        if (!Objects.equals(qnaAnswer.getQnaAnswerStatus(),
            QnaAnswerStatus.ACTIVE)) {
            throw new BudException(INVALID_QNA_ANSWER_STATUS);
        }

        qnaAnswerPinRepository.findByQnaAnswerId(request.getQnaAnswerId())
            .ifPresent(ap -> {
                throw new BudException(CHANGE_IMPOSSIBLE_PINNED_ANSWER);
            });
    }

    private void validateCreateQnaAnswer(Post post) {
        if (!Objects.equals(post.getPostType(), PostType.QNA)) {
            throw new BudException(INVALID_POST_TYPE_FOR_ANSWER);
        }

        if (!Objects.equals(post.getPostStatus(), PostStatus.ACTIVE)) {
            throw new BudException(INVALID_POST_STATUS);
        }
    }

}
