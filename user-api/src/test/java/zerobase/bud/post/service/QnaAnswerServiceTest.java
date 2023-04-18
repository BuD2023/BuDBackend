package zerobase.bud.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static zerobase.bud.common.type.ErrorCode.CHANGE_IMPOSSIBLE_PINNED_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_STATUS;
import static zerobase.bud.common.type.ErrorCode.INVALID_POST_TYPE_FOR_ANSWER;
import static zerobase.bud.common.type.ErrorCode.INVALID_QNA_ANSWER_STATUS;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_POST;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER;
import static zerobase.bud.common.type.ErrorCode.NOT_FOUND_QNA_ANSWER_PIN;
import static zerobase.bud.common.type.ErrorCode.NOT_POST_OWNER;
import static zerobase.bud.post.type.PostStatus.ACTIVE;
import static zerobase.bud.post.type.PostStatus.INACTIVE;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.domain.Member;
import zerobase.bud.notification.domain.NotificationInfo;
import zerobase.bud.notification.service.SendNotificationService;
import zerobase.bud.post.domain.Post;
import zerobase.bud.post.domain.QnaAnswer;
import zerobase.bud.post.domain.QnaAnswerPin;
import zerobase.bud.post.dto.CreateQnaAnswer.Request;
import zerobase.bud.post.dto.UpdateQnaAnswer;
import zerobase.bud.post.repository.PostRepository;
import zerobase.bud.post.repository.QnaAnswerPinRepository;
import zerobase.bud.post.repository.QnaAnswerRepository;
import zerobase.bud.post.type.PostType;
import zerobase.bud.post.type.QnaAnswerStatus;

@ExtendWith(MockitoExtension.class)
class QnaAnswerServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private QnaAnswerRepository qnaAnswerRepository;

    @Mock
    private QnaAnswerPinRepository qnaAnswerPinRepository;

    @Mock
    private SendNotificationService sendNotificationService;

    @InjectMocks
    private QnaAnswerService qnaAnswerService;

    @Test
    void success_createQnaAnswer() {
        //given

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getPost()));

        given(qnaAnswerRepository.save(any()))
            .willReturn(getQnaAnswer());

        ArgumentCaptor<QnaAnswer> captor = ArgumentCaptor.forClass(
            QnaAnswer.class);

        //when
        String answer = qnaAnswerService.createQnaAnswer(getSender(),
            Request.builder()
                .postId(1L)
                .content("content")
                .build());

        //then
        verify(qnaAnswerRepository, times(1)).save(captor.capture());
        assertEquals("postContent", captor.getValue().getPost().getContent());
        assertEquals(1, captor.getValue().getPost().getCommentCount());
        assertEquals("content", captor.getValue().getContent());
        assertEquals(QnaAnswerStatus.ACTIVE,
            captor.getValue().getQnaAnswerStatus());
        assertEquals("sender", answer);

    }

    private NotificationInfo getNotificationInfo() {
        return NotificationInfo.builder()
            .member(getSender())
            .fcmToken("fcmToken")
            .isPostPushAvailable(true)
            .isFollowPushAvailable(true)
            .build();
    }

    @Test
    @DisplayName("NOT_FOUND_POST_createQnaAnswer")
    void NOT_FOUND_POST_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        given(postRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(getSender(),
                Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_POST, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_POST_TYPE_FOR_ANSWER_createQnaAnswer")
    void INVALID_POST_TYPE_FOR_ANSWER_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        Post post = Post.builder()
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.FEED)
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(getSender(),
                Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_POST_TYPE_FOR_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_POST_STATUS_createQnaAnswer")
    void INVALID_POST_STATUS_createQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        Post post = Post.builder()
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(INACTIVE)
            .postType(PostType.QNA)
            .build();

        given(postRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(post));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.createQnaAnswer(getSender(),
                Request.builder()
                    .postId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_POST_STATUS, budException.getErrorCode());
    }

    @Test
    void success_updateQnaAnswer() {
        //given
        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getQnaAnswer()));

        given(qnaAnswerPinRepository.findByQnaAnswerId(anyLong()))
            .willReturn(Optional.empty());

        //when
        Long answer = qnaAnswerService.updateQnaAnswer(
            UpdateQnaAnswer.Request.builder()
                .qnaAnswerId(3L)
                .content("content")
                .build());

        //then
        assertEquals(3L, answer);
    }

    @Test
    @DisplayName("NOT_FOUND_QNA_ANSWER_updateQnaAnswer")
    void NOT_FOUND_QNA_ANSWER_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                UpdateQnaAnswer.Request.builder()
                    .qnaAnswerId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(NOT_FOUND_QNA_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("INVALID_QNA_ANSWER_STATUS_updateQnaAnswer")
    void INVALID_QNA_ANSWER_STATUS_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때
        QnaAnswer qnaAnswer = QnaAnswer.builder()
            .member(getSender())
            .post(getPost())
            .content("content")
            .qnaAnswerStatus(QnaAnswerStatus.INACTIVE)
            .build();

        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswer));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                UpdateQnaAnswer.Request.builder()
                    .qnaAnswerId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(INVALID_QNA_ANSWER_STATUS, budException.getErrorCode());
    }

    @Test
    @DisplayName("CHANGE_IMPOSSIBLE_PINNED_ANSWER_updateQnaAnswer")
    void CHANGE_IMPOSSIBLE_PINNED_ANSWER_updateQnaAnswer() {
        //given 어떤 데이터가 주어졌을 때

        given(qnaAnswerRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(getQnaAnswer()));

        given(qnaAnswerPinRepository.findByQnaAnswerId(anyLong()))
            .willReturn(Optional.of(getQnaAnswerPin()));

        //when 어떤 경우에
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.updateQnaAnswer(
                UpdateQnaAnswer.Request.builder()
                    .qnaAnswerId(1L)
                    .content("content")
                    .build()));
        //then 이런 결과가 나온다.
        assertEquals(CHANGE_IMPOSSIBLE_PINNED_ANSWER,
            budException.getErrorCode());
    }

    @Test
    void success_pinnedQnaAnswer() {
        //given
        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(getPost())
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.ofNullable(QnaAnswer.builder()
                .id(1L)
                .member(getPostMaker())
                .post(post)
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
                .build()));

        given(qnaAnswerPinRepository.save(any()))
            .willReturn(qnaAnswerPin);

        ArgumentCaptor<QnaAnswerPin> captor =
            ArgumentCaptor.forClass(QnaAnswerPin.class);

        //when
        Long answer = qnaAnswerService.qnaAnswerPin(1L, getPostMaker());

        //then
        verify(qnaAnswerPinRepository, times(1)).save(captor.capture());
        assertEquals(PostType.QNA, captor.getValue().getPost().getPostType());
        assertEquals("content", captor.getValue().getQnaAnswer().getContent());
        assertEquals(1L, answer);
    }

    @Test
    @DisplayName("1)NOT_FOUND_QNA_ANSWER_pinnedQnaAnswer")
    void NOT_FOUND_QNA_ANSWER_pinnedQnaAnswer() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class, () ->
            qnaAnswerService.qnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_POST_OWNER_pinnedQnaAnswer")
    void NOT_POST_OWNER_pinnedQnaAnswer() {
        //given
        given(qnaAnswerRepository.findByIdAndQnaAnswerStatus(anyLong(), any()))
            .willReturn(Optional.ofNullable(QnaAnswer.builder()
                .id(1L)
                .post(getPost())
                .member(getPostMaker())
                .content("content")
                .qnaAnswerStatus(QnaAnswerStatus.INACTIVE)
                .build()));

        //when
        BudException budException = assertThrows(BudException.class, () ->
            qnaAnswerService.qnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_POST_OWNER, budException.getErrorCode());
    }

    @Test
    void success_cancelQnaAnswerPin() {
        //given
        Post post = Post.builder()
            .member(getPostMaker())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(post)
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswerPin));

        //when
        Long answer = qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker());

        //then
        assertEquals(1L, answer);
    }

    @Test
    @DisplayName("NOT_FOUND_QNA_ANSWER_PIN_cancelQnaAnswerPin")
    void NOT_FOUND_QNA_ANSWER_PIN_cancelQnaAnswerPin() {
        //given
        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        //when
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_FOUND_QNA_ANSWER_PIN, budException.getErrorCode());
    }

    @Test
    @DisplayName("NOT_POST_OWNER_cancelQnaAnswerPin")
    void NOT_POST_OWNER_cancelQnaAnswerPin() {
        //given
        Post post = Post.builder()
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.QNA)
            .build();

        QnaAnswerPin qnaAnswerPin = QnaAnswerPin.builder()
            .id(1L)
            .post(post)
            .qnaAnswer(getQnaAnswer())
            .build();

        given(qnaAnswerPinRepository.findById(anyLong()))
            .willReturn(Optional.ofNullable(qnaAnswerPin));

        //when
        BudException budException = assertThrows(BudException.class,
            () -> qnaAnswerService.cancelQnaAnswerPin(1L, getPostMaker()));

        //then
        assertEquals(NOT_POST_OWNER, budException.getErrorCode());
    }

    private QnaAnswerPin getQnaAnswerPin() {
        return QnaAnswerPin.builder()
            .post(getPost())
            .qnaAnswer(getQnaAnswer())
            .build();
    }

    private static QnaAnswer getQnaAnswer() {
        return QnaAnswer.builder()
            .id(1L)
            .member(getSender())
            .post(getPost())
            .content("content")
            .qnaAnswerStatus(QnaAnswerStatus.ACTIVE)
            .build();
    }

    private static Post getPost() {
        return Post.builder()
            .member(getSender())
            .title("title")
            .content("postContent")
            .postStatus(ACTIVE)
            .postType(PostType.QNA)
            .build();
    }

    private static Member getPostMaker() {
        return Member.builder()
            .id(3L)
            .nickname("postMaker")
            .userId("postMaker")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Member getSender() {
        return Member.builder()
            .id(1L)
            .nickname("sender")
            .userId("sender")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    private static Member getReceiver() {
        return Member.builder()
            .id(2L)
            .nickname("receiver")
            .userId("receiver")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    }

}